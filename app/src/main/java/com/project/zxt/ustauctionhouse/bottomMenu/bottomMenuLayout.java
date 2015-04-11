package com.project.zxt.ustauctionhouse.bottomMenu;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
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
 */
public class bottomMenuLayout extends LinearLayout {
    //实例化layout使用的类

    private LayoutInflater mInflater;


    //保存菜单按钮的集合，每一个集合元素代表一个按钮，包含了按钮所需要的信息：图片，文字，按键处理事件。

    private List<bottomButton> bottomButtons;


    //封装菜单按钮的布局。

    private View bottomMenuLayout;

    public static final int bottom_layoutHeight = 65;

    public bottomMenuLayout(Context context)
    {
        super(context);
    }

    public bottomMenuLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void processInitButton()

    {

        //初始化布局，将底部菜单layout加入到视图中。

        initLayout(this.getContext());


        //绑定每一个菜单按钮

        bindingButton();


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

        int lessHeight = screenHeight - bottom_layoutHeight;
        params.height = lessHeight;
        customView.setLayoutParams(params);
    }

    private void bindingButton() {
        LinearLayout buttonGroup = (LinearLayout)findViewById(R.id.bottom_menu_button_group_id);

        //初始化底部菜单按钮。
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        if(this.bottomButtons != null && this.bottomButtons.size() > 0)
        {
            //根据按钮数量计算按钮所占的宽度，默认要多加一个主菜单按钮一起算。
            //获取屏幕宽度
            int screenWidth = getScreenSize.getInstance().getScreenWidth();
            layoutParams.width = screenWidth / (bottomButtons.size() + 1);

            //绑定每一个按钮
            for (int i = 0; i < bottomButtons.size(); i++)
            {
                bottomButton oneButton = bottomButtons.get(i);

                //获取底部菜单按钮布局片段。
                View buttonFrameInstance = mInflater.inflate(R.layout.bottom_menu_button_frame, null);
                TextView templateText = (TextView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_text_id);
                templateText.setText(oneButton.getText());
                //templateText.setBackgroundResource(oneButton.getBackgroundResource());

                ImageView templateImg = (ImageView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_img_id);
                templateImg.setImageResource(oneButton.getBackgroundResource());

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


                //设置监听事件。
                buttonFrameInstance.setOnClickListener(oneButton.getClickListener());


                //设置按钮背景的宽度和背景图片
                LinearLayout templateButtonLayout = (LinearLayout)buttonFrameInstance.findViewById(R.id.bottom_menu_template_button_id);
                templateButtonLayout.setLayoutParams(layoutParams);
                if(oneButton.isCurrent())
                    templateButtonLayout.setBackgroundResource(R.drawable.tab_two_highlight);
                else
                    templateButtonLayout.setBackgroundResource(R.drawable.tab_one_normal);
            }
        }

        //默认增加主菜单的按钮
        View buttonFrameInstance = mInflater.inflate(R.layout.bottom_menu_button_frame, null);
        TextView templateText = (TextView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_text_id);
        templateText.setText("me");

        ImageView templateImg = (ImageView)buttonFrameInstance.findViewById(R.id.bottom_menu_template_img_id);
        templateImg.setImageResource(R.drawable.home);
        //templateText.setBackgroundResource(R.drawable.home);

        if(this.bottomButtons != null && this.bottomButtons.size() > 0)
        {
            ImageView fenge = new ImageView(buttonFrameInstance.getContext());
            fenge.setImageResource(R.drawable.line);
            fenge.setLayoutParams(params);

            buttonGroup.addView(fenge);
        }
        buttonGroup.addView(buttonFrameInstance);

        LinearLayout templateButtonLayout = (LinearLayout)buttonFrameInstance.findViewById(R.id.bottom_menu_template_button_id);
        templateButtonLayout.setLayoutParams(layoutParams);
        templateButtonLayout.setBackgroundResource(R.drawable.tab_one_normal);
        buttonFrameInstance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                v.getContext().startActivity(new Intent(v.getContext(),PersonalInformation.class));
            }
        });
    }


    public void setButtonList(List<bottomButton> bottomButtons)
    {
        this.bottomButtons = bottomButtons;
    }
}
