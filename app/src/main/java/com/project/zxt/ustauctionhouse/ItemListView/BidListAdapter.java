/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.ItemListView;




import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;



public class BidListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public BidListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.bid_list_entry, null);

        TextView price = (TextView)vi.findViewById(R.id.bidlist_entry_price); //
        TextView user = (TextView)vi.findViewById(R.id.bidlist_entry_buyer); //
        TextView time = (TextView) vi.findViewById(R.id.bidlist_entry_time);

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值
        price.setText("$"+ goods.get("price"));
        user.setText(goods.get("buyer"));
        time.setText(goods.get("time"));

        return vi;
    }

    public void updateView( ArrayList<HashMap<String, String>> datas ){
        this.data = datas;
        this.notifyDataSetChanged();
    }
}

