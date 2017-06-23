package lcl.android.spider.web.network.activitys;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.util.FontUtil;

/**
 * @author Choi Hwan Soo
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_FOR_PERMISSION = 200;

    private static final String BEFORE_TTS_APP_PACKAGE = "com.navercorp.android.tts";

    boolean readContactsPermission;
    boolean sendSmsPermission;
    boolean receiveSmsPermission;
    boolean readSmsPermission;
    boolean phonePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            readContactsPermission = true;
        }

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



    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                readContactsPermission = true;
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

    private void init() {

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        boolean initProfile = pref.getBoolean("initProfile", false);

        if (initProfile == false) {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (readContactsPermission == false || phonePermission == false || sendSmsPermission == false || receiveSmsPermission == false || readSmsPermission == false) {
                        requestPermission();
                    } else {
                        Intent intent = new Intent(MainActivity.this, NickNameActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }, 1000);
        } else {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (readContactsPermission == false || phonePermission == false || sendSmsPermission == false || receiveSmsPermission == false || readSmsPermission == false) {
                        requestPermission();
                    } else {
                        Intent intent = new Intent(MainActivity.this, GroupListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 1000);
        }

    }

    private void requestPermission() {
        // user에게 permission허용 dialog를 띄우는 역할
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        }, REQUEST_FOR_PERMISSION);
    }

}