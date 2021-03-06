/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.GCM;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class RegisterApp extends AsyncTask<Void, Void, String> {
    private static final String TAG = "GCMRelated";
    private Context ctx;
    private GoogleCloudMessaging gcm;
    private String SENDER_ID = "163181979124";
    private String regId = null;
    private int appVersion;
    private String userKey = null;

    public RegisterApp(String registrationID, Context ctx, GoogleCloudMessaging gcm, int appVersion){
        this.ctx = ctx;
        this.gcm = gcm;
        this.appVersion = appVersion;
        userKey = registrationID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    String msg;
    @Override
    protected String doInBackground(Void... arg0) {

        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(ctx);
            }
            regId = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regId;

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            sendRegistrationIdToBackend();

            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.
            // Persist the regID - no need to register again.


            //storeRegistrationId(ctx, regId);



        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }
    /*
        private void storeRegistrationId(Context ctx, String regId) {
            final SharedPreferences prefs = ctx.getSharedPreferences(Main.class.getSimpleName(),
                    Context.MODE_PRIVATE);
            Log.i(TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("registration_id", regId);
            editor.putInt("appVersion", appVersion);
            editor.apply();

        }
    */
    private void sendRegistrationIdToBackend() {
        NameValuePair pair1 = new BasicNameValuePair("regId", regId);
        List<NameValuePair> pairList = new ArrayList<>();
        pairList.add(pair1);
        try {
            HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
            // URL使用基本URL即可，其中不需要加参数
            HttpPost httpPost = new HttpPost(Utility.serverUrl + "/registerGCM");
            httpPost.addHeader("Authorization",userKey);
            //httpPost.addHeader("Authorization","91c9bfa10ff21db168154fe3ab064b95");
            // 将请求体内容加入请求中
            httpPost.setEntity(requestHttpEntity);
            // 需要客户端对象来发送请求
            HttpClient httpClient = new DefaultHttpClient();
            // 发送请求
            /*HttpResponse response = */httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
        Log.v(TAG, result + " " + appVersion);
    }
}