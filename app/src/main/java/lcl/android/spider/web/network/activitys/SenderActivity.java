package lcl.android.spider.web.network.activitys;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.adapter.MyListAdapter;
import lcl.android.spider.web.network.constants.CommonConstants;
import lcl.android.spider.web.network.model.Contact;
import lcl.android.spider.web.network.model.GroupSetting;
import lcl.android.spider.web.network.receiver.SmsSentReceiver;
import lcl.android.spider.web.network.util.AES256Util;
import lcl.android.spider.web.network.util.FontUtil;

public class SenderActivity extends AppCompatActivity implements View.OnClickListener {

    Button sendButton;
    EditText content;

    private final int REQUEST_FOR_CONTACT = 10;
    private final int REQUEST_FOR_VOICE = 20;
    private final int REQUEST_FOR_PERMISSION = 200;

    private Intent intent;
    private SmsSentReceiver smsSentReceiver = new SmsSentReceiver();
    GroupSetting groupSetting = null;

    String nickName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender_layout);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);

        sendButton = (Button) findViewById(R.id.button_send);

        content = (EditText) findViewById(R.id.content);

        sendButton.setOnClickListener(this);



        ListView receiverListView = (ListView) findViewById(R.id.receiverListView);
        //Intent intent = getIntent();
        String groupName = null;
        if (getIntent() != null) {
            try {
                groupName = getIntent().getStringExtra(GroupListActivity.GROUP_NAME);
                groupSetting = getGroupSetting(groupName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //set up our adapter and attach it to the ListView
        MyListAdapter adapter = new MyListAdapter(this, groupSetting);
        receiverListView.setAdapter(adapter);

    }





    public void onClick(View v) {
        if (v == sendButton) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                Intent sentIntent = new Intent(CommonConstants.SENT_SMS_ACTION);
                Intent deliveryIntent = new Intent(CommonConstants.DELIVERED_SMS_ACTION);
                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent deliveryPpendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                nickName = pref.getString("nickname", "");

                SmsManager smsManager = SmsManager.getDefault();
                String message = createMessage(content.getText().toString(), groupSetting.getGroupName());
                ArrayList<String> partMessage = smsManager.divideMessage(message);
                if (partMessage.size() > 1) { // 보내는 문자열 길이에 따라 보내는 방식이 다름
                    int numParts = partMessage.size();
                    ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                    ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                    for (int i = 0; i < numParts; i++) {
                        sentIntents.add(sentPendingIntent);
                        deliveryIntents.add(deliveryPpendingIntent);
                    }

                    for (Contact contact : groupSetting.getContactList()) {
                        smsManager.sendMultipartTextMessage(contact.getPurePhoneNumber(), null, partMessage, sentIntents, deliveryIntents);
                    }


                } else {

                    for (Contact contact : groupSetting.getContactList()) {
                        smsManager.sendTextMessage(contact.getPurePhoneNumber(), null, message, sentPendingIntent, deliveryPpendingIntent);
                    }

                }

        }
    }

    protected void onResume() {
        smsSentReceiver = new SmsSentReceiver();
        registerReceiver(smsSentReceiver, new IntentFilter("SENT_SMS_ACTION"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (smsSentReceiver != null) {
            unregisterReceiver(smsSentReceiver);
            smsSentReceiver = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (smsSentReceiver != null) {
            unregisterReceiver(smsSentReceiver);
            smsSentReceiver = null;
        }
        super.onDestroy();
    }

    private String createMessage(String message, String groupName) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String curTimeStr = format.format(cal.getTime());
        long curTime = System.currentTimeMillis();
        message += "\n";
        message += "From." + nickName + "," + curTimeStr;
        message += "\n";
        try {
            AES256Util aes256 = new AES256Util(CommonConstants.SECURE_KEY + "*" + curTime);
            String encSendKey = aes256.aesEncode(groupName);
            message += "[" + encSendKey + "," + curTime + "]";
        } catch (UnsupportedEncodingException e) {
            message = "";
        } catch (NoSuchAlgorithmException e) {
            message = "";
        } catch (InvalidKeyException e) {
            message = "";
        } catch (InvalidAlgorithmParameterException e) {
            message = "";
        } catch (NoSuchPaddingException e) {
            message = "";
        } catch (BadPaddingException e) {
            message = "";
        } catch (IllegalBlockSizeException e) {
            message = "";
        }
        return message;
    }

    private GroupSetting getGroupSetting(String groupName) throws IOException {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        GroupSetting groupSetting = new GroupSetting(groupName, null);
        String data = pref.getString(groupSetting.getSharedPreferenceKey(), "");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, GroupSetting.class);
    }


}

