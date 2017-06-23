package lcl.android.spider.web.network.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.model.Constants;

public class GroupListActivity extends AppCompatActivity {
    public static final String GROUP_NAME = "group_name";


    private List<String> groupList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. group 목록을 조회한다.
        groupList = getGroupList();

        // 2. 어뎁터를 설정한다.
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, groupList);

        // 3. 리스트 뷰에 어뎁터를 추가한다.
        ListView listView = (ListView) findViewById(R.id.group_list);
        listView.setAdapter(adapter);

        // 4. 리스트 뷰에 클릭 이벤트를 추가한다.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GroupListActivity.this, GroupSettingActivity.class);
                intent.putExtra(GROUP_NAME, groupList.get(position));
                startActivity(intent);
            }
        });

        // 5. 저장 버튼에 이벤트를 추가한다.
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
        return new LinkedList<String>(Arrays.asList(pref.getString(Constants.GROUP_LIST_KEY, "").split(Constants.GROUP_TOKEN)));
    }

    // group 이름 추가하기
    private void addGroup(String groupName) {
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
