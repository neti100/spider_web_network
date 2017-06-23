package lcl.android.spider.web.network.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.util.FontUtil;

/**
 * @author Choi Hwan Soo
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_REGIST_NPUSH = 100;
    private boolean updateChk = false;

    private static final String BEFORE_TTS_APP_PACKAGE = "com.navercorp.android.tts";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);

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

    private void init() {

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        boolean initProfile = pref.getBoolean("initProfile", false);

        if (initProfile == false) {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Intent intent = new Intent(MainActivity.this, NickNameActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);
        } else {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Intent intent = new Intent(MainActivity.this, GroupListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);
        }

    }

}