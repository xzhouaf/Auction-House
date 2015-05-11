package com.project.zxt.ustauctionhouse.LiveAuction;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paul on 15年5月11日.
 */
public class LiveBidAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public LiveBidAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.live_bid_entry, null);

        TextView price = (TextView)vi.findViewById(R.id.livebid_entry_price); //
        TextView user = (TextView)vi.findViewById(R.id.livebid_entry_user); //
        TextView time = (TextView) vi.findViewById(R.id.livebid_entry_time);

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值

        return vi;
    }

    public void updateView( ArrayList<HashMap<String, String>> datas ){
        this.data = datas;
        this.notifyDataSetChanged();
    }
}