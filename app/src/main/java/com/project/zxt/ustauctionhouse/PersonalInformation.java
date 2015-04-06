package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Xutong on 2015/4/6.
 */
public class PersonalInformation extends Activity implements View.OnClickListener{
    private static final String TAG = "LogIn";
    private LinearLayout personalInfo, myAuction, myBid, myHistory;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.me);

        Log.i(TAG, "Activity: onCreate()");

        personalInfo = (LinearLayout) findViewById(R.id.personal_info);
        personalInfo.setOnClickListener(this);


    }

    public void onClick(View v) {

    }


}
