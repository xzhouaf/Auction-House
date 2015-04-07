package com.project.zxt.ustauctionhouse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    public static final String serverurl = "http://gaozihou.no-ip.org/task_manager/v1";
    public static String DATABASE_PATH = Environment.getExternalStorageDirectory() + "/AuctionHouse";

    public static JSONObject response2obj(HttpResponse response){
        String result = "";
        String line;
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

    public static boolean initializeDirectory(){
        File dir = new File(DATABASE_PATH);
        if (!dir.exists()) {
            if(!dir.mkdir()){
                return false;
            }
        }

        File dir3 = new File(DATABASE_PATH + "/temp");
        if (!dir3.exists()) {
            if(!dir3.mkdir()){
                return false;
            }
        }
        return true;
    }

    public static boolean isOnline(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
        return (user!=null);
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
        return (pass != null && pass.length() >= 6);
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
        return (code==200);
    }
}
