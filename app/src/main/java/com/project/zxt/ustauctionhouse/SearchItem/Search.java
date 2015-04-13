package com.project.zxt.ustauctionhouse.SearchItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

/**
 * Created by Paul on 2015/4/12.
 *
 */

public class Search extends bottomMenuActivity implements View.OnClickListener{
    private static final String TAG = "Search";
    private String UserName, Email, ApiKey, CreatedAt;
    private Intent intent;
    private Context ctx;

    public int getContentViewLayoutResId() { return R.layout.search; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

        Button testToResult = (Button)findViewById(R.id.searchButton);
        testToResult.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ctx, SearchResult.class);
                        startActivity(intent);
                    }
                }
        );

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }

}