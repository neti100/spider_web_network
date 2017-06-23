package lcl.android.spider.web.network.activitys;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.listview.ContactListViewAdapter;
import lcl.android.spider.web.network.model.Constants;
import lcl.android.spider.web.network.model.Contact;
import lcl.android.spider.web.network.model.ContactType;
import lcl.android.spider.web.network.model.GroupSetting;
import lcl.android.spider.web.network.util.FontUtil;

public class GroupSettingActivity extends AppCompatActivity {

    public static final String PHONE_BOOK_LIST = "phoneBookList";
    private static final int PHONE_BOOK_RESULT = 6666;

    private ListView listView;
    private ContactListViewAdapter adapter;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);

        listView = (ListView) findViewById(R.id.contactList);
        adapter = new ContactListViewAdapter(listView);
        listView.setAdapter(adapter);


        Intent intent = getIntent();
        if (intent != null) {
            try {
                groupName = intent.getStringExtra(GroupListActivity.GROUP_NAME);

                if (groupName != null && groupName.equals("") == false) {
                    GroupSetting groupSetting = getGroupSetting(groupName);
                    ((EditText) findViewById(R.id.group_name)).setVisibility(View.GONE);
                    TextView gropuNameTextView = (TextView) findViewById(R.id.group_name_text_view);
                    gropuNameTextView.setVisibility(View.VISIBLE);
                    gropuNameTextView.setText(groupName);
                    adapter.addContact(groupSetting.getContactList());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        Button addContact = (Button) findViewById(R.id.addContact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addContact("", "", ContactType.EDIT);
            }
        });

        Button addPhoneBook = (Button) findViewById(R.id.addPhoneBook);
        addPhoneBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupSettingActivity.this, PhoneBookListActivity.class);

                List<Contact> contactList = adapter.getContactList(ContactType.PHONE_BOOK);
                Contact[] t = new Contact[contactList.size()];

                intent.putExtra(PHONE_BOOK_LIST, contactList.toArray(t));

                startActivityForResult(intent, PHONE_BOOK_RESULT);
            }
        });

        Button saveButton = (Button) findViewById(R.id.saveGroupSetting);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = GroupSettingActivity.this.groupName == null || GroupSettingActivity.this.groupName.equals("") ? ((EditText) findViewById(R.id.group_name)).getText().toString() : GroupSettingActivity.this.groupName;
                List<Contact> contactList = adapter.getContactList();


                if (groupName == null || groupName.equals("")) {
                    Toast.makeText(GroupSettingActivity.this, "그룹 이름을 지정해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                GroupSetting groupSetting = new GroupSetting(groupName, contactList);

                // 그룹 저장
                addGroup(groupName);

                // 그룹 설정내용 저장
                try {
                    addGroupSetting(groupSetting);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                finish();
                ;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHONE_BOOK_RESULT && resultCode == PhoneBookListActivity.RESULT_OK) {
            ArrayList<Contact> result = (ArrayList<Contact>) data.getSerializableExtra(PhoneBookListActivity.PHONE_BOOK_LIST_RESULT);

            // 기존에서 지워진 데이터 삭제
            List<Contact> contactList = adapter.getContactList(ContactType.PHONE_BOOK);
            for (Contact contact : contactList) {
                if (result.contains(contact) == false) {
                    adapter.deleteContact(contact);
                }
            }

            // 새로 추가된 데이터 추가
            for (Contact contact : result) {
                if (contactList.contains(contact) == false) {
                    adapter.addContact(contact);
                }
            }
        }
    }

    // group 이름 불러오기
    private List<String> getGroupList() {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String groupListStr = pref.getString(Constants.GROUP_LIST_KEY, "");
        if(groupListStr.length() == 0) {
            return new LinkedList<String>();
        }
        return new LinkedList<String>(Arrays.asList(groupListStr.split(Constants.GROUP_TOKEN)));
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

    // group setting 추가하기
    private void addGroupSetting(GroupSetting groupSetting) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String data = mapper.writeValueAsString(groupSetting);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(groupSetting.getSharedPreferenceKey(), data);
        editor.commit();
    }

    private GroupSetting getGroupSetting(String groupName) throws IOException {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        GroupSetting groupSetting = new GroupSetting(groupName, null);
        String data = pref.getString(groupSetting.getSharedPreferenceKey(), "");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, GroupSetting.class);
    }
}
