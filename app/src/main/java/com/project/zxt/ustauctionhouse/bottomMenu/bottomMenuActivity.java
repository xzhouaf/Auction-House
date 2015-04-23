package com.project.zxt.ustauctionhouse.bottomMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.PersonalInfo.PersonalInformation;
import com.project.zxt.ustauctionhouse.PostItem.PostItem;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.SearchItem.Search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xutong on 2015/4/11.
 * This is a test for the git
 */
public abstract class bottomMenuActivity extends Activity{

    private LayoutInflater mInflater;   //实例化layout使用的类
    protected bottomMenuLayout bottomMenuLayout;   //底部菜单UI部分
    protected View contentView;                    //页面中间UI部分
    private String current_tab;
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    Intent intent;
    Context ctx;
    //private GestureDetector mDetector;


    private ViewPager mPager;//页卡内容
    private List<View> listViews; // Tab页面列表
    private View realizedViewForChild;
    private int currentSelectedViewPager;
    private int changeToWhichPage;
    private boolean needToSwitchPage = false;

    protected void onCreate(Bundle savedInstanceState)
    {
        intent = getIntent();
        ctx = getApplicationContext();
        //mDetector = new GestureDetector(this, new MyGestureListener());

        //设置标题不显示
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //实例化工具类
        if(getScreenSize.getInstance() == null)
            new getScreenSize(getWindowManager());

        //创建出完整的页面Layout，并设置为当前Activity的页面。
        bottomMenuLayout = new bottomMenuLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomMenuLayout.setOrientation(bottomMenuLayout.VERTICAL);
        bottomMenuLayout.setLayoutParams(layoutParams);
        setContentView(bottomMenuLayout);

        //将业务自定义的layout实例化出来，设置给完整页面Layout的内容部分。其中，获取业务自定义layoutID的时候回调了子类的方法。
        this.mInflater = LayoutInflater.from(this);
        //contentView = mInflater.inflate(getContentViewLayoutResId(), null);
        contentView = mInflater.inflate(R.layout.view_pager_frame, null);
        bottomMenuLayout.addView(contentView);
        InitViewPager();


        //回调子类,正常处理onCreate方法。
        onCreatOverride(savedInstanceState, realizedViewForChild);

        current_tab = intent.getStringExtra("current_tab");
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");

        Log.i("Current Tab: ", current_tab);

        //回调子类，获得所有的底部菜单按钮的集合，并进行处理，将按钮绑定到菜单里。
        bottomMenuLayout.processInitButton(current_tab);

        //Set listeners for the buttons
        View tabViewArray[] = bottomMenuLayout.getButtonArray();
        for(int i = 0; i < tabViewArray.length; i++){
            View buttonFrameInstance = tabViewArray[i];
            if(i == 0){
                buttonFrameInstance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Tag Clicked: ", "  0");
                        onClickTab(0);
                    }
                });
            }
            if(i == 1){
                buttonFrameInstance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Tag Clicked: ", "  1");
                        onClickTab(1);
                    }
                });
            }
            if(i == 2){
                buttonFrameInstance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Tag Clicked: ", "  2");
                        onClickTab(2);
                    }
                });
            }
            if(i == 3){
                buttonFrameInstance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Tag Clicked: ", "  3");
                        onClickTab(3);
                    }
                });
            }
        }
    }

    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        switch(getContentViewLayoutResId()){
            case R.layout.new_item:
                realizedViewForChild = mInflater.inflate(getContentViewLayoutResId(), null);
                listViews.add(realizedViewForChild);
                listViews.add(mInflater.inflate(R.layout.post_item, null));
                currentSelectedViewPager = 0;
                break;
            case R.layout.post_item:
                listViews.add(mInflater.inflate(R.layout.new_item, null));
                realizedViewForChild = mInflater.inflate(getContentViewLayoutResId(), null);
                listViews.add(realizedViewForChild);
                listViews.add(mInflater.inflate(R.layout.search, null));
                currentSelectedViewPager = 1;
                break;
            case R.layout.search:
                listViews.add(mInflater.inflate(R.layout.post_item, null));
                realizedViewForChild = mInflater.inflate(getContentViewLayoutResId(), null);
                listViews.add(realizedViewForChild);
                listViews.add(mInflater.inflate(R.layout.me, null));
                currentSelectedViewPager = 1;
                break;
            case R.layout.me:
                listViews.add(mInflater.inflate(R.layout.search, null));
                realizedViewForChild = mInflater.inflate(getContentViewLayoutResId(), null);
                listViews.add(realizedViewForChild);
                currentSelectedViewPager = 1;
                break;
            default:
                break;
        }
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(currentSelectedViewPager);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            if(currentSelectedViewPager == arg0) return;
            needToSwitchPage = true;
            switch (arg0) {
                case 0:
                    changeToWhichPage = 0;
                    break;
                case 1:
                    changeToWhichPage = 1;
                    break;
                case 2:
                    changeToWhichPage = 2;
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
            if(arg0 == ViewPager.SCROLL_STATE_IDLE && needToSwitchPage) {
                needToSwitchPage = false;
                switch(getContentViewLayoutResId()){
                    case R.layout.new_item:
                        onClickTab(changeToWhichPage);
                        break;
                    case R.layout.post_item:
                        onClickTab(changeToWhichPage);
                        break;
                    case R.layout.search:
                        onClickTab(changeToWhichPage+1);
                        break;
                    case R.layout.me:
                        onClickTab(changeToWhichPage+2);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void onClickTab(int num){
        switch(num){
            case 0:
                if(current_tab.equals("New Item")) return;
                //TODO
                Intent i1 = new Intent(ctx, NewItem.class);
                i1.putExtra("user_name", UserName);
                i1.putExtra("user_email", Email);
                i1.putExtra("user_apiKey", ApiKey);
                i1.putExtra("user_createdAt", CreatedAt);
                i1.putExtra("user_ID", UserID);
                i1.putExtra("current_tab", "New Item");
                startActivity(i1);
                overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            case 1:
                if(current_tab.equals("Post Item")) return;
                //TODO
                Intent i2 = new Intent(ctx, PostItem.class);
                i2.putExtra("user_name", UserName);
                i2.putExtra("user_email", Email);
                i2.putExtra("user_apiKey", ApiKey);
                i2.putExtra("user_createdAt", CreatedAt);
                i2.putExtra("user_ID", UserID);
                i2.putExtra("current_tab", "Post Item");
                startActivity(i2);
                overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            case 2:
                if(current_tab.equals("Search")) return;
                //TODO
                Intent i3 = new Intent(ctx, Search.class);
                i3.putExtra("user_name", UserName);
                i3.putExtra("user_email", Email);
                i3.putExtra("user_apiKey", ApiKey);
                i3.putExtra("user_createdAt", CreatedAt);
                i3.putExtra("user_ID", UserID);
                i3.putExtra("current_tab", "Search");
                startActivity(i3);
                overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            case 3:
                if(current_tab.equals("Me")) return;
                //TODO
                Intent i4 = new Intent(ctx, PersonalInformation.class);
                i4.putExtra("user_name", UserName);
                i4.putExtra("user_email", Email);
                i4.putExtra("user_apiKey", ApiKey);
                i4.putExtra("user_createdAt", CreatedAt);
                i4.putExtra("user_ID", UserID);
                i4.putExtra("current_tab", "Me");
                startActivity(i4);
                overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 为避免子类错误调用，覆盖该方法，并定义为空方法。
     */
    public void setContentView(int layoutResID) { }


    /**
     * 子类实现后，在原来的onCreate方法中内容移到这里来操作。
     * @param savedInstanceState
     */
    protected abstract void onCreatOverride(Bundle savedInstanceState, View realizedView);

    /**
     * 返回layout xml的ID
     * 原本在Activity的onCreate方法中调用的setContentView(R.layout.xxxxLayoutId); 现在从该方法返回。
     * @return
     */
    public abstract int getContentViewLayoutResId();

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
