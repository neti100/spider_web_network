package lcl.android.spider.web.network.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import lcl.android.spider.web.network.R;
import lcl.android.spider.web.network.model.GroupSetting;


public class MyListAdapter extends BaseAdapter {
    private GroupSetting groupSetting;

    public Activity context;

    public LayoutInflater inflater;


    public MyListAdapter(Activity context, GroupSetting groupSetting) {
        this.context = context;
        this.groupSetting = groupSetting;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return groupSetting.getContactList().size();
    }

    public Object getItem(int position) {
        return groupSetting.getContactList().get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView txtViewAlbum;
        TextView txtViewPhotos;
    }

    public View getView(int position, View convertview, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder;
        if (convertview == null) {
            holder = new ViewHolder();
            convertview = inflater.inflate(R.layout.listview_row, null);

            holder.txtViewAlbum = (TextView) convertview.findViewById(R.id.txtViewTitle);
            holder.txtViewPhotos = (TextView) convertview.findViewById(R.id.txtViewDescription);

            convertview.setTag(holder);
        } else {
            holder = (ViewHolder) convertview.getTag();
        }

        holder.txtViewAlbum.setText(groupSetting.getContactList().get(position).getPhoneNumber());
        holder.txtViewPhotos.setText(groupSetting.getContactList().get(position).getName());
        return convertview;
    }
}

