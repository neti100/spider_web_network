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

    boolean sendSmsPermission;
    boolean receiveSmsPermission;
    boolean readSmsPermission;
    boolean phonePermission;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            phonePermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSmsPermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            receiveSmsPermission = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            readSmsPermission = true;
        }

        // 만약 disable로 되어있는 상태라면 runtime error 유발
        // user에게 toast로 알려주고 끝내도 되지만, 강제로 permission을 조정할 수는 없으니
        // permission 허가를 요청하도록하자
        // user가 permission 허가를 하기 위해선 다시 환경설정으로 가기는 불편하니까
        // 내 app에서 dialog로 permission 조정하도록 system dialog로 제공하자
        if (phonePermission == false || sendSmsPermission == false || receiveSmsPermission == false || readSmsPermission == false) {
            requestPermission();
        }


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

    private void requestPermission() {
        // user에게 permission허용 dialog를 띄우는 역할
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        }, REQUEST_FOR_PERMISSION);
    }

    // requestPermission함수를 이용해서 유저에게 permission 조정 dialog를 띄웠다고 하더라도
    // 여전히 유저가 거부했을 수도 있다.
    // dialog를 띄웠다고 끝이 아니라 사후 추적해야 한다.
    // requestPermission 함수에 의한 dialog 작업이 끝나는 순간 자동 호출
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FOR_PERMISSION && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phonePermission = true;
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sendSmsPermission = true;
            }

            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                receiveSmsPermission = true;
            }

            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                readSmsPermission = true;
            }

        }
    }

    public void onClick(View v) {
        if (v == sendButton) {
            if (sendSmsPermission && phonePermission) {
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
            } else {
                requestPermission();
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

