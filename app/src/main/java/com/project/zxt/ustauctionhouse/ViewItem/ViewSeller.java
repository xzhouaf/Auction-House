/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.ViewItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Unit;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ViewSeller extends Activity implements View.OnClickListener {

    private static final String TAG = "View Seller";
    private Intent intent;
    private Context ctx;
    private TextView phoneNumber, Email,userName;
    private String user_id,seller_name;
    private ImageLoader imageLoader;
    private ImageView portrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user);
        Log.i(TAG, "Activity: onCreate()");

        intent = getIntent();
        ctx = getApplicationContext();
        phoneNumber = (TextView) findViewById(R.id.ViewPhoneNumber);
        Email = (TextView) findViewById(R.id.ViewEmailAddress);
        userName = (TextView) findViewById(R.id.ViewUserName);
        user_id=intent.getStringExtra("USER_KEY");
        seller_name = intent.getStringExtra("SELLER_NAME");
        userName.setText(seller_name);
        portrait = (ImageView) findViewById(R.id.me_portrait);

        Log.i("Important::::: ", user_id+"");
        new AsyncGetSingleUser().execute();
        Log.i("Important::::: ", user_id+"");

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    private class AsyncGetSingleUser extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){
            Log.i("Important::::: ", user_id+"kkkk");
        }


        @Override
        protected String doInBackground(String... params) {

            String result = "";

            NameValuePair pair1 = new BasicNameValuePair("user_id", Integer.valueOf(user_id)+"");

            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = null;

                httpPost = new HttpPost(Utility.serverUrl + "/getSingleUser");


                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);

                String line = "";
                HttpEntity receivedEntity = response.getEntity();
                InputStream receivedStream = receivedEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        receivedStream));
                while (null != (line = reader.readLine())) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result){
            JSONObject obj=null;
            Log.i("Important::::: ", user_id+"");
            try {
                obj = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if(obj.getString("error").equals("false")) {

                    if(obj.getString("phoneNumber").equals("null")){
                        phoneNumber.setText("Not available!");

                    }
                    else{
                        phoneNumber.setText(obj.getString("phoneNumber"));
                    }
                    Email.setText(obj.getString("email"));
                    String imageFileName = obj.getString("portrait");

                    imageLoader = new ImageLoader(ctx);
                    imageLoader.DisplayImage(Utility.serverUrlForPicture + "/portrait/" + imageFileName, portrait);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }






        }
    }

}