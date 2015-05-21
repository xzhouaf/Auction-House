/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.bottomMenu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.LiveAuction.LiveListFragment;
import com.project.zxt.ustauctionhouse.PersonalInfo.AppInfo;
import com.project.zxt.ustauctionhouse.PersonalInfo.PersonalInfoFragment;
import com.project.zxt.ustauctionhouse.PostItem.PostItemFragment;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.SearchItem.SearchFragment;

import java.util.ArrayList;
import java.util.List;



public class BottomMenuHome extends FragmentActivity implements View.OnClickListener {

    final String TAG = "Bottom Menu Home";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private static final int DARK_COLOR = 0xffe9e31d, BRIGHT_COLOR = 0xfffdff29;
    Intent intent;
    Context ctx;

    private ViewPager mPager;//页卡内容
    private ArrayList<Fragment> fragments; // Tab页面列表
    private MyPagerAdapter adapter;

    private Fragment newItem, postItem, search, live, me ;
    private OnPassParamListener newItemLis, postItemLis, searchLis, liveLis, meLis;
    private LinearLayout newBut, postBut, searchBut, liveBut, meBut;

    private TextView title;
    private ImageView tiny_icon;
    private long mBackToTopTime;

    int currentSelectedViewPager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_menu_view_pager_frame);
        Log.i(TAG, "Activity: onCreate()");

        intent = getIntent();
        ctx = getApplicationContext();

        title = (TextView)findViewById(R.id.vp_frame_title);
        title.setOnClickListener(this);
        tiny_icon = (ImageView)findViewById(R.id.top_icon_button);
        tiny_icon.setOnClickListener(this);

        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");

        mPager = (ViewPager) findViewById(R.id.internal_view_pager);
        mPager.setOffscreenPageLimit(4);
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

        live = new LiveListFragment();
        fragments.add(live);
        liveLis = (OnPassParamListener) live;
        liveLis.onPassAction(UserName, Email, ApiKey, UserID);

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
        liveBut = (LinearLayout) findViewById(R.id.live_touch_vp);
        liveBut.setOnClickListener(this);
        meBut = (LinearLayout) findViewById(R.id.me_touch_vp);
        meBut.setOnClickListener(this);

        newItemLis.onUpdateAction();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Success1");
        postItemLis.activityResultHandle(requestCode, resultCode, data);
        meLis.activityResultHandle(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnPassParamListener{
        public void onPassAction(String a, String b, String c, String d);
        public void onUpdateAction();
        public void activityResultHandle(int requestCode, int resultCode, Intent data);
        public void onDoubleClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_item_touch_vp:
                if(currentSelectedViewPager != 0){
                    mPager.setCurrentItem(0);
                }
                onClickTab(0);
                break;
            case R.id.top_icon_button:
                Intent intent = new Intent(ctx, AppInfo.class);
                startActivity(intent);
                break;
            case R.id.post_item_touch_vp:
                if(currentSelectedViewPager != 1){
                    mPager.setCurrentItem(1);
                }
                onClickTab(1);
                break;
            case R.id.search_touch_vp:
                if(currentSelectedViewPager != 2){
                    mPager.setCurrentItem(2);
                }
                onClickTab(2);
                break;
            case R.id.live_touch_vp:
                if(currentSelectedViewPager != 3){
                    mPager.setCurrentItem(3);
                }
                onClickTab(3);
                break;
            case R.id.me_touch_vp:
                if(currentSelectedViewPager != 4){
                    mPager.setCurrentItem(4);
                }
                onClickTab(4);
                break;
            case R.id.vp_frame_title:
                if((System.currentTimeMillis() - mBackToTopTime)>500){
                    //goBackTopToast = Toast.makeText(ctx, "Double click back to top", Toast.LENGTH_SHORT);
                    //goBackTopToast.setGravity(Gravity.TOP, 0, 150);
                    //goBackTopToast.show();
                    mBackToTopTime = System.currentTimeMillis();
                }else{
                    //TODO
                    liveLis.onDoubleClick();
                    newItemLis.onDoubleClick();
                }
                break;
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
                case 4:
                    onClickTab(4);
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
                resetTabColor();
                newBut.setBackgroundColor(BRIGHT_COLOR);
                currentSelectedViewPager = 0;
                title.setText("Auction House Home");
                break;
            case 1:
                if(currentSelectedViewPager == 1) return;
                resetTabColor();
                postBut.setBackgroundColor(BRIGHT_COLOR);
                postItemLis.onUpdateAction();
                currentSelectedViewPager = 1;
                title.setText("Post Item");
                break;
            case 2:
                if(currentSelectedViewPager == 2) return;
                resetTabColor();
                searchBut.setBackgroundColor(BRIGHT_COLOR);
                searchLis.onUpdateAction();
                currentSelectedViewPager = 2;
                title.setText("Search Item");
                break;
            case 3:
                if(currentSelectedViewPager == 3) return;
                resetTabColor();
                liveBut.setBackgroundColor(BRIGHT_COLOR);
                liveLis.onUpdateAction();
                currentSelectedViewPager = 3;
                title.setText("Live Auction");
                break;
            case 4:
                if(currentSelectedViewPager == 4) return;
                resetTabColor();
                meBut.setBackgroundColor(BRIGHT_COLOR);
                meLis.onUpdateAction();
                currentSelectedViewPager = 4;
                title.setText("Personal Information");
                break;
            default:
                break;
        }
    }

    private void resetTabColor(){
        newBut.setBackgroundColor(DARK_COLOR);
        postBut.setBackgroundColor(DARK_COLOR);
        searchBut.setBackgroundColor(DARK_COLOR);
        liveBut.setBackgroundColor(DARK_COLOR);
        meBut.setBackgroundColor(DARK_COLOR);
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