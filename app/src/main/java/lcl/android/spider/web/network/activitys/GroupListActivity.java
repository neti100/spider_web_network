package lcl.android.spider.web.network.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.listview.GroupListViewAdapter;
import lcl.android.spider.web.network.model.Constants;
import lcl.android.spider.web.network.model.GroupSetting;
import lcl.android.spider.web.network.util.FontUtil;

public class GroupListActivity extends AppCompatActivity {
    public static final String GROUP_NAME = "group_name";


    private List<String> groupList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView textView = (TextView) findViewById(R.id.nickname);
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String nickname = pref.getString("nickname", "");
        textView.setText(nickname + "님~");


        // 1. group 목록을 조회한다.
        groupList = getGroupList();

        // 2. 어뎁터를 설정한다.
        GroupListViewAdapter adapter = new GroupListViewAdapter(GroupListActivity.this);
        adapter.addGroup(groupList);


        // 3. 리스트 뷰에 어뎁터를 추가한다.
        ListView listView = (ListView) findViewById(R.id.group_list);

        listView.setAdapter(adapter);


        // 4. 추가 버튼에 이벤트를 추가한다.
        Button addGroup = (Button) findViewById(R.id.addGroup);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupListActivity.this, GroupSettingActivity.class);
                startActivity(intent);
            }
        });
    }


    // 값 불러오기
    private List<String> getGroupList() {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String groupListStr = pref.getString(Constants.GROUP_LIST_KEY, "");
        if(groupListStr.length() == 0) {
            return new LinkedList<String>();
        }
        return new LinkedList<String>(Arrays.asList(groupListStr.split(Constants.GROUP_TOKEN)));
    }


    // 값 불러오기
    public void deleteGroupList(String groupName) {
        List<String> groupList = getGroupList();

        groupList.remove(groupName);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.remove(Constants.GROUP_LIST_KEY);

        for (String s : groupList) {
            addGroup(s);
        }

        GroupSetting groupSetting = new GroupSetting();
        editor.remove(groupSetting.getSharedPreferenceKey());

        editor.commit();
    }


    // group 이름 추가하기
    private void addGroup(String groupName) {
        String param = getIntent().getStringExtra(GroupListActivity.GROUP_NAME);
        if (param != null && param.equals("") == false) { // 기존에 저장되어있는 이름. 추가할 필요 없음
            return;
        }

        List<String> groupList = getGroupList();
        groupList.add(groupName);

        String t = "";

        for (String group : groupList) {
            t += group + Constants.GROUP_TOKEN;
        }

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(Constants.GROUP_LIST_KEY, t);
        editor.commit();
    }

}
