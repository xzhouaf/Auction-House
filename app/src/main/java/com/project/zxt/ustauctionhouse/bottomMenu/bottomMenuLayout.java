package com.project.zxt.ustauctionhouse.bottomMenu;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.project.zxt.ustauctionhouse.PersonalInformation;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.getScreenSize;

import java.util.List;

/**
 * Created by Xutong on 2015/4/11.
 *
 */
public class bottomMenuLayout extends LinearLayout {

    //Added by GAO. All of them are important parameters
    private final int numOfTabs = 4;
    private final String[] tabNames = {"New Item", "Post Item", "Search", "Me"};
    //实例化layout使用的类
    private LayoutInflater mInflater;

    //封装菜单按钮的布局。
    private View bottomMenuLayout;
    public static final int bottom_layoutHeight = 58;
    private View[] buttonArray;

    public bottomMenuLayout(Context context)
    {
        super(context);
    }

    public bottomMenuLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void processInitButton(String current_tab)
    {
        //初始化布局，将底部菜单layout加入到视图中。
        initLayout(this.getContext());

        //绑定每一个菜单按钮
        bindingButton(current_tab);

        //重新计算整个布局的大小，使用整个屏幕的高度减去底部菜单的高度，
        //得出并设置中间页面部分的高度，就能够将菜单固定在底部。
        resizeLayout();
    }

    private void initLayout(Context context)
    {
        this.mInflater = LayoutInflater.from(context);
        bottomMenuLayout = mInflater.inflate(R.layout.bottom_menu_layout, null);
        addView(bottomMenuLayout);
    }

    private void resizeLayout()
    {
        View customView = getChildAt(0);
        android.view.ViewGroup.LayoutParams params = customView.getLayoutParams();
        int screenHeight = getScreenSize.getInstance().getScreenHeight();

        int lessHeight = (int)(screenHeight - bottom_layoutHeight*getResources().getDisplayMetrics().density);
        params.height = lessHeight;
        customView.setLayoutParams(params);
    }

    private void bindingButton(String current_tab) {

        LinearLayout buttonGroup = (LinearLayout)findViewById(R.id.bottom_menu_button_group_id);

        //初始化底部菜单按钮。
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        if(true)
        {
            //根据按钮数量计算按钮所占的宽度，默认要多加一个主菜单按钮一起算。
            //获取屏幕宽度
            int screenWidth = getScreenSize.getInstance().getScreenWidth();
            layoutParams.width = screenWidth / numOfTabs;
            buttonArray = new View[numOfTabs];

            //绑定每一个按钮
            for (int i = 0; i <numOfTabs; i++)
            {
                //获取底部菜单按钮布局片段。
                final View buttonFrameInstance = mInflater.inflate(R.layout.bottom_menu_button_frame, null);

                TextView templateText = (TextView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_text_id);
                templateText.setText(tabNames[i]);
                ImageView templateImg = (ImageView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_img_id);
                templateImg.setImageResource(R.drawable.home);

                //如果不是第一个按钮，则需要增加分割线
                if(i != 0)
                {
                    ImageView fenge = new ImageView(buttonFrameInstance.getContext());
                    fenge.setImageResource(R.drawable.line);
                    fenge.setLayoutParams(params);
                    buttonGroup.addView(fenge);
                }

                //将按钮增加到菜单栏
                buttonGroup.addView(buttonFrameInstance);

                //设置按钮背景的宽度和背景图片
                LinearLayout templateButtonLayout = (LinearLayout)buttonFrameInstance.findViewById(R.id.bottom_menu_template_button_id);
                templateButtonLayout.setLayoutParams(layoutParams);
                if(tabNames[i].equals(current_tab)) {
                    templateButtonLayout.setBackgroundResource(R.drawable.tab_two_highlight);
                }else {
                    templateButtonLayout.setBackgroundResource(R.drawable.tab_one_normal);
                }
                buttonArray[i] = buttonFrameInstance;
            }
        }
    }

    public View[] getButtonArray(){
        return buttonArray;
    }
}
