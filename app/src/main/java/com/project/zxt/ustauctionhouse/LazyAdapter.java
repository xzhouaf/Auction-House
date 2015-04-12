package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 2015/4/12.
 *
 */

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.search_entry, null);

        TextView name = (TextView)vi.findViewById(R.id.EntryItemName); //
        TextView seller = (TextView)vi.findViewById(R.id.EntrySellerName); //
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.EntryItemImage); // 缩略图

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值
        name.setText(goods.get(SearchResult.KEY_NAME));
        seller.setText(goods.get(SearchResult.KEY_SELLER));
        imageLoader.DisplayImage(goods.get(SearchResult.KEY_IMAGE), thumb_image);
        return vi;
    }
}