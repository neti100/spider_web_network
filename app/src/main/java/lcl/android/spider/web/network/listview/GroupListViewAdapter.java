package lcl.android.spider.web.network.listview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.activitys.GroupListActivity;
import lcl.android.spider.web.network.activitys.GroupSettingActivity;
import lcl.android.spider.web.network.activitys.SenderActivity;

/**
 * Created by NAVER on 2017-06-23.
 */

public class GroupListViewAdapter extends BaseAdapter {

    private List<String> groupSettingList = new ArrayList<>();
    private Context context;

    public GroupListViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return groupSettingList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupSettingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_list_item, parent, false);
        }

        TextView groupName = (TextView) convertView.findViewById(R.id.group_name_text);
        Button sendButton = (Button) convertView.findViewById(R.id.group_send_button);
        Button modifyButton = (Button) convertView.findViewById(R.id.group_modify_button);

        groupName.setText(groupSettingList.get(position));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupListViewAdapter.this.context, SenderActivity.class);
                intent.putExtra(GroupListActivity.GROUP_NAME, groupSettingList.get(position));
                GroupListViewAdapter.this.context.startActivity(intent);
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupListViewAdapter.this.context, GroupSettingActivity.class);
                intent.putExtra(GroupListActivity.GROUP_NAME, groupSettingList.get(position));
                GroupListViewAdapter.this.context.startActivity(intent);
            }
        });

        return convertView;
    }


    public void addGroup(List<String> gropuNameList) {
        this.groupSettingList.addAll(gropuNameList);
    }
}
