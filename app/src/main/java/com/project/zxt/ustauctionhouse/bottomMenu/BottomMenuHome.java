package com.project.zxt.ustauctionhouse.bottomMenu;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.PersonalInfo.PersonalInfoFragment;
import com.project.zxt.ustauctionhouse.PersonalInfo.PersonalInformation;
import com.project.zxt.ustauctionhouse.PostItem.PostItem;
import com.project.zxt.ustauctionhouse.PostItem.PostItemFragment;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.SearchItem.Search;
import com.project.zxt.ustauctionhouse.SearchItem.SearchFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 15年4月26日.
 *
 */

public class BottomMenuHome extends FragmentActivity implements View.OnClickListener {

    final String TAG = "Bottom Menu Home";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    Intent intent;
    Context ctx;

    private ViewPager mPager;//页卡内容
    private ArrayList<Fragment> fragments; // Tab页面列表
    private MyPagerAdapter adapter;

    private Fragment newItem, postItem, search, me;
    private OnPassParamListener newItemLis, postItemLis, searchLis, meLis;
    private LinearLayout newBut, postBut, searchBut, meBut;

    int currentSelectedViewPager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_menu_view_pager_frame);
        Log.i(TAG, "Activity: onCreate()");

        intent = getIntent();
        ctx = getApplicationContext();

        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");

        mPager = (ViewPager) findViewById(R.id.internal_view_pager);
        mPager.setOffscreenPageLimit(3);
        fragments = new ArrayList<Fragment>();

        newItem = new NewItemFragment();
        fragments.add(newItem);
        newItemLis = (OnPassParamListener) newItem;
        newItemLis.onPassAction(UserName, Email, ApiKey, UserID);

        postItem = new PostItemFragment();
        fragments.add(postItem);
        postItemLis = (OnPassParamListener) postItem;
        postItemLis.onPassAction(UserName, Email, ApiKey, UserID);

        search = new SearchFragment();
        fragments.add(search);
        searchLis = (OnPassParamListener) search;
        searchLis.onPassAction(UserName, Email, ApiKey, UserID);

        me = new PersonalInfoFragment();
        fragments.add(me);
        meLis = (OnPassParamListener) me;
        meLis.onPassAction(UserName, Email, ApiKey, UserID);

        adapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());

        newBut = (LinearLayout) findViewById(R.id.new_item_touch_vp);
        newBut.setOnClickListener(this);
        postBut = (LinearLayout) findViewById(R.id.post_item_touch_vp);
        postBut.setOnClickListener(this);
        searchBut = (LinearLayout) findViewById(R.id.search_touch_vp);
        searchBut.setOnClickListener(this);
        meBut = (LinearLayout) findViewById(R.id.me_touch_vp);
        meBut.setOnClickListener(this);

        newItemLis.onUpdateAction();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Success1");
        postItemLis.activityResultHandle(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnPassParamListener{
        public void onPassAction(String a, String b, String c, String d);
        public void onUpdateAction();
        public void activityResultHandle(int requestCode, int resultCode, Intent data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            default:
                break;
        }
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        public List<Fragment> fragments = new ArrayList<Fragment>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyPagerAdapter(FragmentManager fm, ArrayList<Fragment> fs) {
            super(fm);
            this.fragments = fs;
        }

        public Fragment getItem(int index){
            return fragments.get(index);
        }

        public int getCount(){
            return fragments.size();
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Log.i(TAG, "Page Selected: "+arg0);
            if(currentSelectedViewPager == arg0) return;
            switch (arg0) {
                case 0:
                    onClickTab(0);
                    break;
                case 1:
                    onClickTab(1);
                    break;
                case 2:
                    onClickTab(2);
                    break;
                case 3:
                    onClickTab(3);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    }

    private void onClickTab(int num){
        switch(num){
            case 0:
                if(currentSelectedViewPager == 0) return;

                currentSelectedViewPager = 0;
                break;
            case 1:
                if(currentSelectedViewPager == 1) return;
                postItemLis.onUpdateAction();
                currentSelectedViewPager = 1;
                break;
            case 2:
                if(currentSelectedViewPager == 2) return;
                searchLis.onUpdateAction();
                currentSelectedViewPager = 2;
                break;
            case 3:
                if(currentSelectedViewPager == 3) return;
                meLis.onUpdateAction();
                currentSelectedViewPager = 3;
                break;
            default:
                break;
        }
    }

    private long mExitTime;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "Double click to exit UST Auction", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}