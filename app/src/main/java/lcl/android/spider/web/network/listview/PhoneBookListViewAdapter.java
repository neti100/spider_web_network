package lcl.android.spider.web.network.listview;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.model.Contact;

/**
 * Created by NAVER on 2017-06-20.
 */

public class PhoneBookListViewAdapter extends BaseAdapter {

    private Context context;
    private List<Contact> list;

    public PhoneBookListViewAdapter(Context context, List<Contact> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();


        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.phone_book_list_item, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
        TextView phoneNumberTextView = (TextView) convertView.findViewById(R.id.phone_number);
        final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

        final Contact phoneBook = list.get(position);
        nameTextView.setText(phoneBook.getName());
        phoneNumberTextView.setText(phoneBook.getPhoneNumber());
        checkBox.setChecked(phoneBook.isChecked());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneBook.setChecked(phoneBook.isChecked() == false);
            }
        });

        return convertView;
    }
}
