package com.project.zxt.ustauctionhouse;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Paul on 2015/4/6.
 */
public class Utility {

    public static final String serverurl = "http://gaozihou.no-ip.org/task_manager/v1";
    public static String DATABASE_PATH = Environment.getExternalStorageDirectory() + "/AuctionHouse";

    public static JSONObject response2obj(HttpResponse response){
        String result = "";
        String line = "";
        JSONObject obj = null;
        try {
            HttpEntity receivedEntity = response.getEntity();
            InputStream receivedStream = receivedEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    receivedStream));
            while (null != (line = reader.readLine())) {
                result += line;
            }
            obj = new JSONObject(result);
        }catch(Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    public static void initializeDirectory(){
        File dir = new File(DATABASE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File dir3 = new File(DATABASE_PATH + "/temp");
        if (!dir3.exists()) {
            dir3.mkdir();
        }
    }

    public static boolean isOnline(Context ctx) {

        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static boolean isValidUserName(String user) {
        if (user != null ) {
            return true;
        }
        return false;
    }
    // validating email id
    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // validating password with retype password
    public static boolean isValidPassword(String pass) {
        if (pass != null && pass.length() >= 6) {
            return true;
        }
        return false;
    }

    public static boolean serverTest(){
        HttpGet httpGet = new HttpGet(serverurl + "/serverTest");
        HttpClient httpClient = new DefaultHttpClient();
        Integer code = 0;
        try{
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine a = response.getStatusLine();
            code = a.getStatusCode();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(code == 200) return true;
        return false;
    }
}
