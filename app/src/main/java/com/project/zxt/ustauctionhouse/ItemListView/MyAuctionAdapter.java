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
import android.widget.ImageView;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class MyAuctionAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public MyAuctionAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.view_my_selling_entry, null);

        TextView name = (TextView)vi.findViewById(R.id.sellEntryItemName); //
        TextView highest_bidder = (TextView)vi.findViewById(R.id.sellEntryHighestBidderName); //
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.sellEntryItemImage); // 缩略图
        TextView time_left = (TextView) vi.findViewById(R.id.sellEntryTimeLeft);
        TextView current_price = (TextView) vi.findViewById(R.id.sellEntryPrice);

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值
        name.setText(goods.get(Utility.KEY_NAME));
        current_price.setText(goods.get(Utility.KEY_CURRENT_PRICE));
        time_left.setText(goods.get(Utility.KEY_TIME_LEFT));
        highest_bidder.setText(goods.get(Utility.KEY_HIGHEST_BIDDER));
        if(goods.get(Utility.KEY_HIGHEST_BIDDER).equals("No bidder yet")){
            vi.setBackgroundColor(0xffffe9d9);
        }else{
            vi.setBackgroundColor(0xffcbffa1);
        }
        imageLoader.DisplayImage(goods.get(Utility.KEY_IMAGE), thumb_image);
        return vi;
    }
    public void updateView( ArrayList<HashMap<String, String>> datas ){
        this.data = datas;
        this.notifyDataSetChanged();
    }

}
