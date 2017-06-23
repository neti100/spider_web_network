package lcl.android.spider.web.network.activitys;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.listview.PhoneBookListViewAdapter;
import lcl.android.spider.web.network.model.Contact;
import lcl.android.spider.web.network.model.ContactType;
import lcl.android.spider.web.network.util.FontUtil;

public class PhoneBookListActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 8888;
    public static final int RESULT_OK = 7777;
    public static final String PHONE_BOOK_LIST_RESULT = "phoneBookList";


    private ArrayList<Contact> phoneBookList = null;


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    // 해당 권한을 사용해서 작업을 진행할 수 있습니다
                    paintView();
                } else {
                    // 권한 거부
                    // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                    finish();

                }
                return;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_book_list);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
        FontUtil.setGlobalFont(getWindow().getDecorView(), tf);


        ///// 권한 있는지 확인
        // Activity에서 실행하는경우
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // 이 권한을 필요한 이유를 설명해야하는가?
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
//
//                // 다이어로그같은것을 띄워서 사용자에게 해당 권한이 필요한 이유에 대해 설명합니다
//                // 해당 설명이 끝난뒤 requestPermissions()함수를 호출하여 권한허가를 요청해야 합니다
//
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//                // 필요한 권한과 요청 코드를 넣어서 권한허가요청에 대한 결과를 받아야 합니다
//            }
        } else {
            paintView();
        }
    }

    private void paintView() {
        phoneBookList = getPhoneBookList();
        PhoneBookListViewAdapter phoneBookListViewAdapter = new PhoneBookListViewAdapter(PhoneBookListActivity.this, phoneBookList);

        ListView listView = (ListView) findViewById(R.id.phone_book_list);
        listView.setAdapter(phoneBookListViewAdapter);
    }

    @Override
    public void onBackPressed() {
        if (this.phoneBookList == null) {
            finish();
            return;
        }

        ArrayList<Contact> data = new ArrayList<>();


        for (Contact contact : this.phoneBookList) {
            if (contact.isChecked()) {
                data.add(contact);
            }
        }

        Intent intent = getIntent();
        intent.putExtra(PHONE_BOOK_LIST_RESULT, data);
        setResult(RESULT_OK, intent);
        finish();
    }

    private ArrayList<Contact> getPhoneBookList() {
        Parcelable[] t = getIntent().getParcelableArrayExtra(GroupSettingActivity.PHONE_BOOK_LIST);
        List<Contact> inputPhoneNumberList = new ArrayList<>();
        if (t != null) {
            inputPhoneNumberList.addAll(Arrays.asList(Arrays.copyOf(t, t.length, Contact[].class)));
        }

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

        int ididx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameidx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

        ArrayList<Contact> result = new ArrayList<>();
        String name = null;
        String phoneNumber = null;
        while (cursor.moveToNext()) {
            name = cursor.getString(nameidx);

            // 전화번호는 서브 쿼리로 조사해야 함.
            String id = cursor.getString(ididx);
            Cursor cursor2 = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

            int typeidex = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int numidx = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


            // 전화의 타입에 따라 여러 개가 존재한다.
            while (cursor2.moveToNext()) {

                if (cursor2.getInt(typeidex) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    phoneNumber = cursor2.getString(numidx);
                    break;
                }
            }
            cursor2.close();

            result.add(new Contact(phoneNumber, cursor.getString(nameidx), shouldCheck(inputPhoneNumberList, phoneNumber), ContactType.PHONE_BOOK));
        }
        cursor.close();

        return result;
    }

    private boolean shouldCheck(List<Contact> inputPhoneNumberList, String phoneNumber) {
        boolean result = false;
        for (Contact contact : inputPhoneNumberList) {
            if (phoneNumber.equals(contact.getPhoneNumber())){
                return true;
            }
        }

        return result;
    }

}
