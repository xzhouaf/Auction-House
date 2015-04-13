package com.project.zxt.ustauctionhouse.bottomMenu;

import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Xutong on 2015/4/11.
 */
public class getScreenSize {

        private static getScreenSize instance;

        private int screenHeight;
        private int screenWidth;

        public getScreenSize(WindowManager windowManager)
        {
            instance = this;
            Display display = windowManager.getDefaultDisplay();
            this.screenHeight = display.getHeight(); //屏幕高度
            this.screenWidth = display.getWidth();  //屏幕宽度
        }
        public static getScreenSize getInstance()
        {
            return instance;
        }


        /**
         * 获得屏幕的高度。
         * @return
         */
        public int getScreenHeight() {
            return screenHeight;
        }

        /**
         * 获得屏幕的宽度。
         * @return
         */
        public int getScreenWidth() {
            return screenWidth;
        }



}
