package lcl.android.spider.web.network.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lcl.android.spider.web.network.constants.CommonConstants;
import lcl.android.spider.web.network.model.Constants;
import lcl.android.spider.web.network.model.Contact;
import lcl.android.spider.web.network.model.GroupSetting;
import lcl.android.spider.web.network.util.AES256Util;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by CHS on 2017-06-19.
 */
public class SmsReceiver extends BroadcastReceiver {
    final String SENT_SMS_ACTION 			= 	"SENT_SMS_ACTION";
    final String DELIVERED_SMS_ACTION 		= 	"DELIVERED_SMS_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences("test", Context.MODE_PRIVATE);

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            Object[] messages = (Object[])bundle.get("pdus");
            SmsMessage[] smsMessage = new SmsMessage[messages.length];

            String message = "";
            for (int i = 0; i < messages.length; i++) {
                smsMessage[i] = SmsMessage.createFromPdu((byte[])messages[i]);
                message += smsMessage[i].getMessageBody().toString();
            }

            String secureKey = ""; // 암호화된 키
            String sendTime = ""; // 발송시각
            String ePattern = "(\\[(.+)?\\s,([\\d]+)\\])"; // 암호화 정보를 얻기위한 패턴
            Pattern p = Pattern.compile(ePattern);
            Matcher m = p.matcher(message);


            if (m.find()) {
                int lastIdx = m.groupCount();
                secureKey = m.group(lastIdx - 1);
                sendTime = m.group(lastIdx);

            }

            if (secureKey.isEmpty() || sendTime.isEmpty()) {
                return;
            }

            String groupName = "";
            // 복호화
            try {
                AES256Util aes256 = new AES256Util(CommonConstants.SECURE_KEY + "*" + sendTime);
                groupName = aes256.aesDecode(secureKey);
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            } catch (InvalidKeyException e) {
            } catch (InvalidAlgorithmParameterException e) {
            } catch (NoSuchPaddingException e) {
            } catch (BadPaddingException e) {
            } catch (IllegalBlockSizeException e) {
            }

            List<String> befGroupNameList = getGroupList(context);
            if (befGroupNameList.contains(groupName) == false) {
                addGroup(groupName, context);
                try {
                    addGroupSetting(new GroupSetting(groupName, new ArrayList<Contact>(), true), context);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            List<String> befReceiveHistList = getReceiveHistoryList(context);
            boolean isReceiveHist = befReceiveHistList.contains(secureKey + sendTime);

            boolean autoSend = false;
            try {
                autoSend = getGroupSetting(groupName, context).isAutoSend();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isReceiveHist == false && autoSend) { // 존재하지 않는다면 전파 수행
                addReceiveHistory(secureKey + sendTime, context);

                TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
                Intent sentIntent = new Intent(SENT_SMS_ACTION);
                Intent deliveryIntent = new Intent(DELIVERED_SMS_ACTION);
                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent deliveryPpendingIntent = PendingIntent.getBroadcast(context, 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> partMessage = smsManager.divideMessage(message);
                if (partMessage.size() > 1) {
                    int numParts = partMessage.size();
                    ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                    ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                    for (int i = 0; i < numParts; i++) {
                        sentIntents.add(sentPendingIntent);
                        deliveryIntents.add(deliveryPpendingIntent);
                    }

                    try {
                        GroupSetting groupSetting = getGroupSetting(groupName, context);
                        for(Contact contact  : groupSetting.getContactList()) {
                            smsManager.sendMultipartTextMessage(contact.getPurePhoneNumber(), null, partMessage, sentIntents, deliveryIntents);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        GroupSetting groupSetting = getGroupSetting(groupName, context);
                        for(Contact contact  : groupSetting.getContactList()) {
                            smsManager.sendTextMessage(contact.getPurePhoneNumber(), null, message, sentPendingIntent, deliveryPpendingIntent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }


    // 값 불러오기
    private List<String> getGroupList(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        String groupListStr = pref.getString(Constants.GROUP_LIST_KEY, "");
        if(groupListStr.length() == 0) {
            return new LinkedList<String>();
        }
        return new LinkedList<String>(Arrays.asList(groupListStr.split(Constants.GROUP_TOKEN)));
    }

    // group 이름 추가하기
    private void addGroup(String groupName, Context context) {
        List<String> groupList = getGroupList(context);
        groupList.add(groupName);

        String t = "";

        for (String group : groupList) {
            t += group + Constants.GROUP_TOKEN;
        }

        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(Constants.GROUP_LIST_KEY, t);
        editor.commit();
    }

    // group setting 추가하기
    private void addGroupSetting(GroupSetting groupSetting, Context context) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String data = mapper.writeValueAsString(groupSetting);

        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(groupSetting.getSharedPreferenceKey(), data);
        editor.commit();
    }

    private GroupSetting getGroupSetting(String groupName, Context context) throws IOException {
        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        GroupSetting groupSetting = new GroupSetting(groupName, null);
        String data = pref.getString(groupSetting.getSharedPreferenceKey(), "");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, GroupSetting.class);
    }


    // 값 불러오기
    private List<String> getReceiveHistoryList(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        String receiveHistStr = pref.getString(Constants.RECEIVE_HIST_KEY, "");
        if(receiveHistStr.length() == 0) {
            return new LinkedList<String>();
        }
        return new LinkedList<String>(Arrays.asList(receiveHistStr.split(Constants.RECEIVE_HIST_TOKEN)));
    }

    // group 이름 추가하기
    private void addReceiveHistory(String addReceiveHistory, Context context) {
        List<String> receiveHistoryList = getReceiveHistoryList(context);
        receiveHistoryList.add(addReceiveHistory);

        String t = "";

        for (String receiveHistory : receiveHistoryList) {
            t += receiveHistory + Constants.RECEIVE_HIST_TOKEN;
        }

        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(Constants.RECEIVE_HIST_KEY, t);
        editor.commit();
    }




}
