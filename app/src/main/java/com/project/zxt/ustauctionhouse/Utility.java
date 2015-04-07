package com.project.zxt.ustauctionhouse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Paul on 2015/4/6.
 */
public class Utility {

    public static final String serverurl = "http://gaozihou.no-ip.org/task_manager/v1";

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
}
