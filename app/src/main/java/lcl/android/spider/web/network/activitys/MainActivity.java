package lcl.android.spider.web.network.activitys;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
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
import android.widget.TextView;

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
import lcl.android.spider.web.network.receiver.SmsSentReceiver;
import lcl.android.spider.web.network.util.AES256Util;

public class MainActivity extends AppCompatActivity implements View.OnClickListener { 

    Button sendButton;
    Button contactButton;
    TextView receiver;
    EditText content;

    boolean contactPermission;
    boolean sendSmsPermission;
    boolean receiveSmsPermission;
    boolean readSmsPermission;
    boolean phonePermission;

    private final int REQUEST_FOR_CONTACT = 10;
    private final int REQUEST_FOR_VOICE = 20;
    private final int REQUEST_FOR_PERMISSION = 200;

    private Intent intent;
    private SmsSentReceiver smsSentReceiver = new SmsSentReceiver();
    private MyListAdapter dataAdapter = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button_send);
        contactButton = (Button) findViewById(R.id.button_contacts);

        content = (EditText) findViewById(R.id.content);

        sendButton.setOnClickListener(this);
        contactButton.setOnClickListener(this);

        // api level 22까지는(5.1) manifest에 <uses-permission>으로 등록만 하면 실행되는 개발자 신고제
        // 23(6.0) 부터는 개발자가 manifest에 아무리 등록했다고 하더라도 user가 환경설정에서 permission enable/disable 조절가능
        // manifest에 등록했다고 끝이 아니라 코드에스 그 부분을 실행할 때 권한획득여부 체크 필수
        // contactPermission = smsPermission = phonePermission = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED) {
            contactPermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED) {
            phonePermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED) {
            sendSmsPermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)== PackageManager.PERMISSION_GRANTED) {
            receiveSmsPermission = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)== PackageManager.PERMISSION_GRANTED) {
            readSmsPermission = true;
        }

            // 만약 disable로 되어있는 상태라면 runtime error 유발
        // user에게 toast로 알려주고 끝내도 되지만, 강제로 permission을 조정할 수는 없으니
        // permission 허가를 요청하도록하자
        // user가 permission 허가를 하기 위해선 다시 환경설정으로 가기는 불편하니까
        // 내 app에서 dialog로 permission 조정하도록 system dialog로 제공하자
        if (contactPermission == false || phonePermission == false || sendSmsPermission == false || receiveSmsPermission == false || readSmsPermission == false) {
            requestPermission();
        }

    }

    private void requestPermission() {
        // user에게 permission허용 dialog를 띄우는 역할
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_CONTACTS,
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
                contactPermission = true;
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                phonePermission = true;
            }

            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                sendSmsPermission = true;
            }

            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                receiveSmsPermission = true;
            }

            if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                readSmsPermission = true;
            }

        }
    }

    public void onClick(View v) {
        if (v == contactButton) {
            if (contactPermission) {
                // 주소록의 목록화면을 띄운다.
                Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://com.android.contacts/data/phones"));
                // 결과를 돌려 받아야 한다.
                startActivityForResult(intent, REQUEST_FOR_CONTACT);
            } else {
                requestPermission();
            }
        } else if(v == sendButton) {
            if (sendSmsPermission && phonePermission) {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                Intent sentIntent = new Intent(CommonConstants.SENT_SMS_ACTION);
                Intent deliveryIntent = new Intent(CommonConstants.DELIVERED_SMS_ACTION);
                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent deliveryPpendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                SmsManager smsManager = SmsManager.getDefault();
                String message =  createMessage(content.getText().toString());
                ArrayList<String> partMessage = smsManager.divideMessage(message);
                if (partMessage.size() > 1) { // 보내는 문자열 길이에 따라 보내는 방식이 다름
                    int numParts = partMessage.size();
                    ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                    ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                    for (int i = 0; i < numParts; i++) {
                        sentIntents.add(sentPendingIntent);
                        deliveryIntents.add(deliveryPpendingIntent);
                    }
                    smsManager.sendMultipartTextMessage(receiver.getText().toString(), null, partMessage, sentIntents, deliveryIntents);
                } else {
                    smsManager.sendTextMessage(receiver.getText().toString(), null, message, sentPendingIntent, deliveryPpendingIntent);
                }
            } else {
                requestPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_CONTACT && resultCode == RESULT_OK) {
            String id = Uri.parse(data.getDataString()).getLastPathSegment();
            Cursor cursor = getContentResolver().query(
                            ContactsContract.Data.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.Data._ID + '=' + id,
                            null,
                            null);
             // row 선택
            cursor.moveToFirst();   // 어차피 해당 id를 갖는 1개의 연락처만 선택됨
            // data추출 및 화면 출력


            ListView receiverListView = (ListView) findViewById(R.id.receiverListView);

            ArrayList<String> receiverPhoneList = new ArrayList<String>();
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));
            receiverPhoneList.add(cursor.getString(0));

            ArrayList<String> receiverNameList = new ArrayList<String>();
            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");
            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");            receiverNameList.add("영희");
            receiverNameList.add("철희");
            receiverNameList.add("바둑이");


            //set up our adapter and attach it to the ListView
            MyListAdapter adapter = new MyListAdapter(this, receiverPhoneList, receiverNameList);
            receiverListView.setAdapter(adapter);

        } else if(requestCode == REQUEST_FOR_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            content.setText(results.get(0));
        }
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(smsSentReceiver, new IntentFilter("SENT_SMS_ACTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsSentReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsSentReceiver);
    }

    private String createMessage(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String curTimeStr = format.format(cal.getTime());
        long  curTime = System.currentTimeMillis();
        message += "\n";
        message += "From." + CommonConstants.SENDER + "," + curTimeStr;
        message += "\n";
        try {
            AES256Util aes256 = new AES256Util(CommonConstants.SECURE_KEY);
            String sendKey = CommonConstants.GROUP_NAME + "*" + curTime;
            String encSendKey = aes256.aesEncode(sendKey);
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


}

