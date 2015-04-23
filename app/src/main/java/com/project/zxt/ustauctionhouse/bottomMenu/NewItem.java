package com.project.zxt.ustauctionhouse.bottomMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.NewListView.RefreshListView;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Paul on 2015/4/12.
 *
 */
public class NewItem extends bottomMenuActivity implements View.OnClickListener, Observer, RefreshListView.OnRefreshListener, RefreshListView.OnLoadListener {
    private static final String TAG = "New Item";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private RefreshListView refreshLv;
    private GeneralSearch search;
    private ArrayList<HashMap<String, String>> paramList;
    private TextView goBackToTop;
    private long mBackToTopTime;

    public int getContentViewLayoutResId() {
        return R.layout.new_item;
    }

    final protected void onCreatOverride(Bundle savedInstanceState, View vw) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");



        goBackToTop = (TextView) vw.findViewById(R.id.new_item_frame_title);
        goBackToTop.setOnClickListener(this);


        refreshLv = (RefreshListView) vw.findViewById(R.id.new_item_listview);
        refreshLv.setOnRefreshListener(this);
        refreshLv.setOnLoadListener(this);
        refreshLv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("See why bug: ", position+"");
                        if(position > paramList.size() || position <= 0) return;
                        Intent intent = new Intent(ctx, ViewItem.class);
                        intent.putExtra(Utility.KEY_IMAGE, paramList.get(position - 1).get(Utility.KEY_IMAGE));
                        intent.putExtra(Utility.KEY_ID, paramList.get(position - 1).get(Utility.KEY_ID));
                        intent.putExtra("user_ID", UserID);
                        intent.putExtra("API_key", ApiKey);
                        startActivity(intent);
                    }
                }
        );

        isRefreshing = true;
        search = new GeneralSearch("0", "", "", "", "", "");
        search.addObserver(this);
        search.loadList();

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    //private Toast goBackTopToast = null;
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_item_frame_title:
                if((System.currentTimeMillis() - mBackToTopTime)>500){
                    //goBackTopToast = Toast.makeText(ctx, "Double click back to top", Toast.LENGTH_SHORT);
                    //goBackTopToast.setGravity(Gravity.TOP, 0, 150);
                    //goBackTopToast.show();
                    mBackToTopTime = System.currentTimeMillis();
                }else{
                    //goBackTopToast.cancel();
                    refreshLv.smoothScrollToPosition(0);
                }
                break;
            default:
                break;
        }
    }


    private LazyAdapter adapter;

    @Override
    public void update(Observable observable, Object data) {
        if (observable == search) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            prepareDataForDisplay(true);
            adapter = new LazyAdapter(this, dataToDisplay);
            search.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            if (isRefreshing) {
                isRefreshing = false;
                refreshLv.refreshComplete();
                if(dataToDisplay.size() < 5){
                    refreshLv.loadComplete(true);
                }else{
                    refreshLv.loadComplete(false);
                }
            }
        }
    }

    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
    private int lastLoad = 0;
    private boolean isToEnd = false;

    private boolean prepareDataForDisplay(boolean isRefresh) {
        if (isRefresh) {
            dataToDisplay.clear();
            lastLoad = 0;
            isToEnd = false;
        }
        if(isToEnd) return false;

        for (int i = 0; i < 5; i++) {
            if (lastLoad + 1 > paramList.size()) {
                isToEnd = true;
                return false;
            }
            dataToDisplay.add(paramList.get(lastLoad));
            lastLoad++;
        }
        return true;
    }

    private void refreshLoadData() {
        search = new GeneralSearch("0", "", "", "", "", "");
        search.addObserver(this);
        search.loadList();
        refreshLv.loadComplete(false);
    }

    private void augmentLoadData() {
        prepareDataForDisplay(false);
        if (isToEnd) {
            refreshLv.loadComplete(true);
            return;
        }

        adapter.updateView(dataToDisplay);
        if(dataToDisplay.size() == paramList.size())
            refreshLv.loadComplete(true);
        else refreshLv.loadComplete(false);
    }

    boolean isRefreshing = false;

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                isRefreshing = true;
                refreshLoadData();

            }
        }, 1000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                augmentLoadData();
            }
        }, 500);
    }








}