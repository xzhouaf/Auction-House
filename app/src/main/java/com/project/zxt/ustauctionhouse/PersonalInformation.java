package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Xutong on 2015/4/6.
 * To be finished by Xutong
 */

public class PersonalInformation extends bottomMenuActivity implements View.OnClickListener{
    private static final String TAG = "Personal Information";
    private LinearLayout personalInfo, myAuction, myBid, myHistory;
    private String UserName, Email, ApiKey, CreatedAt;
    private Intent intent;
    private Context ctx;
    private ImageView portrait;

    public int getContentViewLayoutResId() { return R.layout.me; }

    final protected void onCreatOverride(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.me);

        Log.i(TAG, "Subclass Activity: onCreate()");

        personalInfo = (LinearLayout) findViewById(R.id.personal_info);
        personalInfo.setOnClickListener(this);

        myAuction = (LinearLayout) findViewById(R.id.myAuction);
        myAuction.setOnClickListener(this);

        myBid = (LinearLayout) findViewById(R.id.myBid);
        myBid.setOnClickListener(this);

        myHistory = (LinearLayout) findViewById(R.id.myHistory);
        myHistory.setOnClickListener(this);

        Button logoutBut = (Button) findViewById(R.id.logoutButton);
        logoutBut.setOnClickListener(this);

        Button updateBut = (Button) findViewById(R.id.update);
        updateBut.setOnClickListener(this);

        portrait = (ImageView) findViewById(R.id.portrait);

                intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);
        updatePortrait();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutButton:
                //These are just for eliminating the warnings
                Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);
                Log.i(TAG, personalInfo.toString() +  myAuction.toString() + myBid.toString() +  myHistory.toString());
                onLogoutPressed();
                break;
            case R.id.personal_info:
                Intent i = new Intent(getBaseContext(), ModifyPersonalInfo.class);
                i.putExtra("api_key", ApiKey);
                startActivityForResult(i, 1);
                break;
            case R.id.update:
                updatePortrait();
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if(resultCode==RESULT_CANCELED)
                setTitle("cancle");
            else if (resultCode==RESULT_OK) {
                updatePortrait();
            }
        }
    }

    private void onLogoutPressed(){
        new AlertDialog.Builder(this).setTitle("Logout")
                .setIcon(R.drawable.hhh)
                .setMessage("Are you sure to logout now?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncLogout().execute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                }).show();
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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    SharedPreferences.Editor prefed = prefs.edit();
                    prefed.putString("loggedInA", null);
                    prefed.putString("loggedInB", null);
                    prefed.putString("loggedInC", null);
                    prefed.putString("loggedInD", null);
                    prefed.apply();
                    Intent i = new Intent(ctx, LogIn.class);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void updatePortrait(){
        new AsyncDownloadPortrait().execute();
    }

    private class AsyncDownloadPortrait extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            String url1 = Utility.serverUrl + "/getPortrait";
            HttpGet httpGet = new HttpGet(url1);
            HttpClient httpClient = new DefaultHttpClient();
            httpGet.addHeader("Authorization",ApiKey);
            JSONObject obj = null;
            String fileName = "";
            try{
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine a = response.getStatusLine();
                Log.i("IMPORTANT", "GET request: Status code = " + a.getStatusCode());
                obj = Utility.response2obj(response);
                if(obj.getBoolean("error") == true){
                    return null;
                }else{
                    fileName = obj.getString("file_name");
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            Bitmap bitmap = null;
            try{
                URL url = new URL(Utility.serverUrl + "/portrait/" + fileName);
                HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream inputStream=conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            if(result != null) {
                portrait.setImageBitmap(result);
            }else{
                portrait.setImageDrawable(getResources().getDrawable(R.drawable.hhh));
            }
        }
    }



}
