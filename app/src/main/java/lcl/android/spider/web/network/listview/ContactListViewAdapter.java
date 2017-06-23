package lcl.android.spider.web.network.listview;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.model.Contact;
import lcl.android.spider.web.network.model.ContactType;

/**
 * Created by NAVER on 2017-06-20.
 */

public class ContactListViewAdapter extends BaseAdapter {

    private List<Contact> contactList = new ArrayList<>();
    private ListView listView;

    public ContactListViewAdapter(ListView listView) {
        this.listView = listView;
    }

    @Override
    public int getViewTypeCount() {
        return ContactType.values().length;
    }

    // position 위치의 아이템 타입 리턴.
    @Override
    public int getItemViewType(int position) {
        return contactList.get(position).getType().ordinal();
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contactList.get(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int pos = listView.getPositionForView(parent);
        final Context context = parent.getContext();
        int viewType = getItemViewType(position);
        final ViewHolder viewHolder = convertView == null ? new ViewHolder() : (ViewHolder) convertView.getTag();

        Contact contact = contactList.get(position);

        int contactType = contact.getType().ordinal();



        Button deleteButton = viewHolder.deleteButton;
        TextView textView = viewHolder.textView;
        EditText editText = viewHolder.editText;

        if (convertView == null) {
            viewHolder.contact = contact;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (viewType == ContactType.PHONE_BOOK.ordinal()) {
                convertView = inflater.inflate(R.layout.contact_phone_book_item, parent, false);

                textView = (TextView) convertView.findViewById(R.id.checked_phone_number);
                viewHolder.textView = textView;

                deleteButton = (Button) convertView.findViewById(R.id.delete_phone_number_button);


            } else if (viewType == ContactType.EDIT.ordinal()) {
                convertView = inflater.inflate(R.layout.contact_edit_item, parent, false);

                editText = (EditText) convertView.findViewById(R.id.phone_number_edit_text);
                editText.setTag(position);
                viewHolder.editText = editText;
                viewHolder.editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (contactList.size() <= position) {
                            return;
                        }

                        viewHolder.contact.setPhoneNumber(s.toString());

                    }
                });

                deleteButton = (Button) convertView.findViewById(R.id.delete_edit_number_button);

            }

            viewHolder.deleteButton = deleteButton;
            viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteContact(position);
                }
            });
            convertView.setTag(viewHolder);
        }

        if (viewType == ContactType.PHONE_BOOK.ordinal()) {
            viewHolder.textView.setText(viewHolder.contact.getPhoneNumber() + " (" + viewHolder.contact.getName() + ")");

        } else if (viewType == ContactType.EDIT.ordinal()) {
            viewHolder.editText.setText(viewHolder.contact.getPhoneNumber());
        }

        return convertView;
    }


    public void addContact(String phoneNumber, String name, ContactType contactType) {
        contactList.add(new Contact(phoneNumber, name, contactType));
        notifyDataSetChanged();
        listView.setSelection(contactList.size() - 1);
    }

    public void addContact(Contact contact) {
        contactList.add(contact);
        notifyDataSetChanged();
        listView.setSelection(contactList.size() - 1);
    }


    public void addContact(List<Contact> contactList) {
        this.contactList.addAll(contactList);
        notifyDataSetChanged();
        listView.setSelection(this.contactList.size() - 1);
    }

    public void deleteContact(Contact contact) {
        contactList.remove(contact);
        notifyDataSetChanged();
    }

    public void deleteContact(int i) {
        contactList.remove(i);
        notifyDataSetChanged();
    }

    public List<Contact> getContactList() {
        return this.contactList;
    }

    public List<Contact> getContactList(ContactType type) {
        List<Contact> result = new ArrayList<>();

        for (Contact contact : this.contactList) {
            if (contact.getType() == type) {
                result.add(contact);
            }
        }

        return result;
    }

    static class ViewHolder {
        TextView textView;
        EditText editText;
        Button deleteButton;
        Contact contact;
    }

}
