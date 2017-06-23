package lcl.android.spider.web.network.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import lcl.android.spider.web.network.R;


public class MyListAdapter extends BaseAdapter {
    public ArrayList<String> album_names;
    public ArrayList<String> photos;

    public Activity context;

    public LayoutInflater inflater;

    public MyListAdapter(Activity context, ArrayList<String> album_names, ArrayList<String> photos) {
        super();
        this.context = context;
        this.album_names = album_names;
        this.photos = photos;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return album_names.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
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

        holder.txtViewAlbum.setText(album_names.get(position));
        holder.txtViewPhotos.setText(photos.get(position));
        return convertview;
    }
}

