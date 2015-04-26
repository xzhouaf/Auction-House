package com.project.zxt.ustauctionhouse.bottomMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.NewListView.RefreshListView;
import com.project.zxt.ustauctionhouse.PersonalInfo.PersonalInformation;
import com.project.zxt.ustauctionhouse.PostItem.PostItem;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.SearchItem.Search;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by paul on 15年4月26日.
 *
 */

public class NewItemFragment extends Fragment implements BottomMenuHome.OnPassParamListener,
        View.OnClickListener, Observer, RefreshListView.OnRefreshListener, RefreshListView.OnLoadListener {

    private static final String TAG = "New Item";
    private String UserName, Email, ApiKey, UserID;
    private Intent intent;
    private Context ctx;
    private RefreshListView refreshLv;
    private GeneralSearch search;
    private ArrayList<HashMap<String, String>> paramList;
    private TextView goBackToTop;
    private long mBackToTopTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.i(TAG, "Fragment View: onCreate()");

        View rootView = inflater.inflate(R.layout.new_item, container, false);
        ctx = getActivity().getApplicationContext();
        intent = getActivity().getIntent();

        goBackToTop = (TextView) rootView.findViewById(R.id.new_item_frame_title);
        goBackToTop.setOnClickListener(this);


        refreshLv = (RefreshListView) rootView.findViewById(R.id.new_item_listview);
        refreshLv.setOnRefreshListener(this);
        refreshLv.setOnLoadListener(this);
        refreshLv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("See why bug: ", position + "");
                        if (position > paramList.size() || position <= 0) return;
                        Intent intent = new Intent(ctx, ViewItem.class);
                        intent.putExtra(Utility.KEY_IMAGE, paramList.get(position - 1).get(Utility.KEY_IMAGE));
                        intent.putExtra(Utility.KEY_ID, paramList.get(position - 1).get(Utility.KEY_ID));
                        intent.putExtra("user_ID", UserID);
                        intent.putExtra("API_key", ApiKey);
                        startActivity(intent);
                    }
                }
        );

        return rootView;
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
            adapter = new LazyAdapter(getActivity(), dataToDisplay);
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

    @Override
    public void onPassAction(String a, String b, String c, String d) {
        UserName = a;
        Email = b;
        ApiKey = c;
        UserID = d;
    }

    @Override
    public void onUpdateAction() {
        isRefreshing = true;
        search = new GeneralSearch("0", "", "", "", "", "");
        search.addObserver(this);
        search.loadList();
    }

    @Override
    public void activityResultHandle(int requestCode, int resultCode, Intent data) {

    }
}
