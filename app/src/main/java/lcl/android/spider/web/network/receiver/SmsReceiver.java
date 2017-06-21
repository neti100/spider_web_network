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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lcl.android.spider.web.network.constants.CommonConstants;
import lcl.android.spider.web.network.util.AES256Util;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by CHS on 2017-06-19.
 */
public class SmsReceiver extends BroadcastReceiver {

    final String SENT_SMS_ACTION 			= 	"SENT_SMS_ACTION";
    final String DELIVERED_SMS_ACTION 		= 	"DELIVERED_SMS_ACTION";
    final String numbers[] = {"01054234214","01041859056", "01094149177"};

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

            String secureGroupName = ""; // 암호화된 그룹명
            String uniqueKey = ""; // 발송에 대한 유니크 키
            String ePattern = "(\\[(.+)?\\s,[\\d]+\\])"; // 암호화 정보를 얻기위한 패턴
            Pattern p = Pattern.compile(ePattern);
            Matcher m = p.matcher(message);

            if (m.find()) {
                int lastIdx = m.groupCount();
                secureGroupName = m.group(lastIdx);
                uniqueKey = m.group(1);
            }

            String groupName = "";
            // 복호화
            try {
                AES256Util aes256 = new AES256Util(CommonConstants.SECURE_KEY);
                groupName = aes256.aesDecode(secureGroupName);
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            } catch (InvalidKeyException e) {
            } catch (InvalidAlgorithmParameterException e) {
            } catch (NoSuchPaddingException e) {
            } catch (BadPaddingException e) {
            } catch (IllegalBlockSizeException e) {
            }

            if (CommonConstants.GROUP_NAME.equals(groupName) == false) { // 그룹명이 존재하지 않으면 리턴
                return;
            }

            String befKey = prefs.getString(CommonConstants.PREF_MESSAGE_KEY, ""); // 저장된 발송 키(들) 획득
            if (uniqueKey.equals(befKey) == false) { // 존재하지 않는다면 전파 수행
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(CommonConstants.PREF_MESSAGE_KEY, uniqueKey);
                editor.commit();

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

                    for(String number : numbers) {
                        smsManager.sendMultipartTextMessage(number, null, partMessage, sentIntents, deliveryIntents);
                    }
                } else {
                    for(String number : numbers) {
                        smsManager.sendTextMessage(number, null, message, sentPendingIntent, deliveryPpendingIntent);
                    }
                }

            }

        }
    }

}
