package com.project.zxt.ustauctionhouse.bottomMenu;

import android.view.View;

/**
 * Created by Xutong on 2015/4/11.
 */
public class bottomButton {

    /**
     * 按钮菜单文字
     */
    private String text;

    /**
     * 按钮菜单图片
     */
    private int backgroundResource;

    /**
     * 点击事件。
     */
    private View.OnClickListener clickListener;

    /**
     * 是否当前已经选中的按钮，如果是则高亮，并且忽略点击事件。
     */
    private boolean isCurrent = false;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public int getBackgroundResource() {
        return backgroundResource;
    }

    public void setBackgroundResource(int backgroundResource) {
        this.backgroundResource = backgroundResource;
    }

    public View.OnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }


}
