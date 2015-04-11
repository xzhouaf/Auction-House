package com.project.zxt.ustauctionhouse;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
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

            DisplayMetrics dm = new DisplayMetrics();
            Display display = windowManager.getDefaultDisplay();
            display.getMetrics(dm);

            this.screenHeight = dm.heightPixels; //屏幕高度
            this.screenWidth = dm.widthPixels;  //屏幕宽度
            Log.i("SB", this.screenHeight+"");
            Log.i("SB", this.screenWidth+"");
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
