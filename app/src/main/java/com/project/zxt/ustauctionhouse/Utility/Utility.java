package com.project.zxt.ustauctionhouse.Utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    public static final String serverUrl = "http://gaozihou.no-ip.org:8000/task_manager/v1";
    public static String DATABASE_PATH = Environment.getExternalStorageDirectory() + "/AuctionHouse";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME= "name";
    public static final String KEY_SELLER = "seller";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_CURRENT_PRICE = "current_price";
    public static final String KEY_TIME_LEFT = "time_left";

    public static List<Unit> string2unit(String result) throws JSONException {
        Log.i("Important::::: ", result);
        JSONObject obj = new JSONObject(result);
        String error = obj.getString("error");
        if(error.equals("true")){
            return null;
        }
        if(!obj.isNull("tasks")){
            JSONArray taskArray = obj.getJSONArray("tasks");
            List<Unit> taskInfoArrayList = new ArrayList<Unit>();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            taskInfoArrayList = Arrays.asList(gson.fromJson(taskArray.toString(), Unit[].class));
            Log.i("IMPORTANT:::::length: ", ""+taskInfoArrayList.size());
            //Log.i("IMPORTANT:::::name: ", taskInfoArrayList.get(0).name);
            //Log.i("IMPORTANT:::::price: ", taskInfoArrayList.get(0).currentPrice+"");
            //Log.i("IMPORTANT:::::name: ", taskInfoArrayList.get(0).timeLimit+"");
            return taskInfoArrayList;
        }
        return null;
    }

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

    public static double formatDouble(double d) {
        return (double)Math.round(d*100)/100;
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
        HttpGet httpGet = new HttpGet(serverUrl + "/serverTest");
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

    public static String secondsToTime(int s){
        int day = s/(3600*24);
        int hour = (s%(3600*24))/3600;
        int minute = ((s%(3600*24))%3600)/60;
        int second = ((s%(3600*24))%3600)%60;

        String convert =  day + "d " + hour +"h "+minute+"m "+second+"s";
        return convert;

    }
}

