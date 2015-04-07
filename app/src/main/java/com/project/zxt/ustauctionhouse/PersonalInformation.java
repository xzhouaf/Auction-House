package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xutong on 2015/4/6.
 * To be finished by Xutong
 */

public class PersonalInformation extends Activity implements View.OnClickListener{
    private static final String TAG = "Personal Information";
    private LinearLayout personalInfo, myAuction, myBid, myHistory;
    private Button logoutBut;
    private String UserName, Email, ApiKey, CreatedAt;
    Intent intent;
    Context ctx;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.me);

        Log.i(TAG, "Activity: onCreate()");

        personalInfo = (LinearLayout) findViewById(R.id.personal_info);
        personalInfo.setOnClickListener(this);

        myAuction = (LinearLayout) findViewById(R.id.myAuction);
        myAuction.setOnClickListener(this);

        myBid = (LinearLayout) findViewById(R.id.myBid);
        myBid.setOnClickListener(this);

        myHistory = (LinearLayout) findViewById(R.id.myHistory);
        myHistory.setOnClickListener(this);

        logoutBut = (Button) findViewById(R.id.logoutButton);
        logoutBut.setOnClickListener(this);

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutButton:
                new AsyncLogout().execute();
                break;
            default:
                break;
        }
    }

    private class AsyncLogout extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            // 使用GET方法发送请求,需要把参数加在URL后面，用？连接，参数之间用&分隔
            String url1 = Utility.serverUrl + "/logout";
            // 生成请求对象
            HttpGet httpGet = new HttpGet(url1);
            HttpClient httpClient = new DefaultHttpClient();
            //添加header信息
            httpGet.addHeader("Authorization", ApiKey);
            // 发送请求
            try{
                HttpResponse response = httpClient.execute(httpGet);
                obj = Utility.response2obj(response);
            }catch (Exception e){
                e.printStackTrace();
            }
            return obj;
        }

        protected void onPostExecute(JSONObject result){
            super.onPostExecute(result);
            if(result == null){
                Toast.makeText(ctx, "Cannot connect to server now. Make sure you connect to " +
                        "SMobileNet and the server is turned on!", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Toast.makeText(ctx, result.getString("message"), Toast.LENGTH_SHORT).show();
                if(result.getString("error").equals("false")){
                    Intent i = new Intent(ctx, LogIn.class);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
