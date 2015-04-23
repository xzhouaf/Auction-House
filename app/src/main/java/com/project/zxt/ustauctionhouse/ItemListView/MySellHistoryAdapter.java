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

/**
 * Created by Paul on 2015/4/21.
 */
public class MySellHistoryAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public MySellHistoryAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.history_entry_seller, null);

        TextView name = (TextView)vi.findViewById(R.id.sHistoryEntryItemName); //
        TextView buyer = (TextView)vi.findViewById(R.id.sHistoryBuyerName); //
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.sHistoryItemImage); // 缩略图
        TextView time_complete = (TextView) vi.findViewById(R.id.sHistoryTime);
        TextView current_price = (TextView) vi.findViewById(R.id.sHistoryEntryPrice);

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值
        name.setText(goods.get(Utility.KEY_NAME));

        current_price.setText(goods.get(Utility.KEY_CURRENT_PRICE));
        imageLoader.DisplayImage(goods.get(Utility.KEY_IMAGE), thumb_image);
        return vi;
    }
    public void updateView( ArrayList<HashMap<String, String>> datas ){
        this.data = datas;
        this.notifyDataSetChanged();
    }

}