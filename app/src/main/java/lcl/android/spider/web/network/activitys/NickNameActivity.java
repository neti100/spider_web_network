package lcl.android.spider.web.network.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.util.FontUtil;

/**
 * @author Choi Hwan Soo
 */
public class NickNameActivity extends AppCompatActivity {
    private static final String TAG = NickNameActivity.class.getSimpleName();

    public static final int REQUEST_REGIST_NPUSH = 100;
    private boolean updateChk = false;

    private static final String BEFORE_TTS_APP_PACKAGE = "com.navercorp.android.tts";
    EditText inputNickname;
    Button saveNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nickname_layout);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);

        inputNickname = (EditText)findViewById(R.id.input_nickname);

        inputNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = inputNickname.getText().toString();
                if(text.equals(getApplicationContext().getString(R.string.nickname_hint))) {
                    inputNickname.setText("");
                }
            }
        });


        saveNickname = (Button)findViewById(R.id.save_nickname);




        setSaveNickname();
    }

    @Override
    protected void onResume() {
        super.onResume();
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


    private void setSaveNickname() {

        saveNickname.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveNickname();
                Intent intent = new Intent(NickNameActivity.this, GroupListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveNickname() {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nickname", inputNickname.getText().toString());
        editor.putBoolean("initProfile", true);
        editor.commit();
    }




}

