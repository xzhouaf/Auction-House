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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paul on 15年5月11日.
 */
public class LiveListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public LiveListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
        TextView status = (TextView) vi.findViewById(R.id.entryTimeLeft);
        TextView current_price = (TextView) vi.findViewById(R.id.searchEntryPrice);
        TextView status_title = (TextView) vi.findViewById(R.id.timeTag);

        HashMap<String, String> goods = new HashMap<String, String>();
        goods = data.get(position);

        // 设置ListView的相关值

        status_title.setText("Status: ");
        name.setText(goods.get(Utility.KEY_NAME));
        seller.setText(goods.get(Utility.KEY_SELLER));
        status.setText(goods.get(Utility.KEY_LIVE_STATUS));
        current_price.setText(goods.get(Utility.KEY_CURRENT_PRICE));
        imageLoader.DisplayImage(goods.get(Utility.KEY_IMAGE), thumb_image);
        return vi;
    }

    public void updateView( ArrayList<HashMap<String, String>> datas ){
        this.data = datas;
        this.notifyDataSetChanged();
    }
}
