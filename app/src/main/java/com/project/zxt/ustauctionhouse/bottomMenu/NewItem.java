package com.project.zxt.ustauctionhouse.bottomMenu;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.ItemListView.LazyMyBidAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.RefreshListView;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Paul on 2015/4/12.
 *
 */
public class NewItem extends bottomMenuActivity implements View.OnClickListener, Observer, RefreshListView.OnRefreshListener{
    private static final String TAG = "New Item";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private RefreshListView refreshLv;
    private GeneralSearch search;
    private ArrayList<HashMap<String, String>> paramList;

    public int getContentViewLayoutResId() { return R.layout.new_item; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");

        refreshLv = (RefreshListView) findViewById(R.id.new_item_listview);
        refreshLv.setOnRefreshListener(this);
        refreshLv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ctx, ViewItem.class);
                        intent.putExtra(Utility.KEY_IMAGE, paramList.get(position-1).get(Utility.KEY_IMAGE));
                        intent.putExtra(Utility.KEY_ID, paramList.get(position-1).get(Utility.KEY_ID));
                        intent.putExtra("user_ID", UserID);
                        intent.putExtra("API_key", ApiKey);
                        startActivity(intent);
                    }
                }
        );

        search = new GeneralSearch("0","","","","","");
        search.addObserver(this);
        search.loadList();

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }



    @Override
    public void update(Observable observable, Object data) {
        if(observable == search){
            paramList = (ArrayList<HashMap<String, String>>) data;
            LazyAdapter adapter = new LazyAdapter(this, paramList);
            search.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            refreshLv.refreshComplete();
        }
    }

    private void initLoadData() {
        search = new GeneralSearch("0","","","","","");
        search.addObserver(this);
        search.loadList();
    }


    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                initLoadData();

            }
        }, 10);
    }
}