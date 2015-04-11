package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Xutong on 2015/4/11.
 *
 */
public abstract class bottomMenuActivity extends Activity{

    private LayoutInflater mInflater;   //实例化layout使用的类
    protected bottomMenuLayout bottomMenuLayout;   //底部菜单UI部分
    protected View contentView;                    //页面中间UI部分
    private String current_tab;
    private String UserName, Email, ApiKey, CreatedAt;
    Intent intent;
    Context ctx;
    //private GestureDetector mDetector;

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
        contentView = mInflater.inflate(getContentViewLayoutResId(), null);
        bottomMenuLayout.addView(contentView);

        //回调子类,正常处理onCreate方法。
        onCreatOverride(savedInstanceState);

        current_tab = intent.getStringExtra("current_tab");
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

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
    protected abstract void onCreatOverride(Bundle savedInstanceState);

    /**
     * 返回layout xml的ID
     * 原本在Activity的onCreate方法中调用的setContentView(R.layout.xxxxLayoutId); 现在从该方法返回。
     * @return
     */
    public abstract int getContentViewLayoutResId();

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        this.mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
                               float velocityY) {

            // gesture is from left to right
            if (velocityX > 100) {
                switch(current_tab){
                    case "Post Item":
                        onClickTab(0);
                        break;
                    case "Search":
                        onClickTab(1);
                        break;
                    case "Me":
                        onClickTab(2);
                        break;
                    default:
                        break;
                }
            }
            //from right to left
            if (velocityX < 100) {
                switch(current_tab){
                    case "New Item":
                        onClickTab(1);
                        break;
                    case "Post Item":
                        onClickTab(2);
                        break;
                    case "Search":
                        onClickTab(3);
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

    }
    */

}
