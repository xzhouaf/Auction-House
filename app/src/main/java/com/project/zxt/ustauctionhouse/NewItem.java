package com.project.zxt.ustauctionhouse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Paul on 2015/4/12.
 *
 */
public class NewItem extends bottomMenuActivity implements View.OnClickListener{
    private static final String TAG = "New Item";
    private String UserName, Email, ApiKey, CreatedAt;
    private Intent intent;
    private Context ctx;
    private TextView testingText;

    public int getContentViewLayoutResId() { return R.layout.new_item; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

        testingText = (TextView) findViewById(R.id.newItemTextview);
        new XutongAsyncTask().execute();

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

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

    private class XutongAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String tempString = null;
            for(int i = 0; i < 1000; i++){
                tempString += "Xutong...   ";
            }
            tempString += "This is the last line";
            return tempString;
        }

        protected void onPostExecute(String result){
            testingText.setText(result);
        }
    }
}