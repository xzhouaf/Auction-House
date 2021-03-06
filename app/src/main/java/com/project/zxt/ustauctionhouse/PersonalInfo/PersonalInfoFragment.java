/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.PersonalInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.LiveAuction.WebSocketLive;
import com.project.zxt.ustauctionhouse.LoginRelated.LogIn;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.BottomMenuHome;

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
import java.util.Observable;
import java.util.Observer;


public class PersonalInfoFragment extends Fragment implements View.OnClickListener, Observer, BottomMenuHome.OnPassParamListener {
    private static final String TAG = "Personal Information";
    private LinearLayout personalInfo, myAuction, myBid, myHistory;
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private ImageView portrait;
    private TextView name;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");
        View vw = inflater.inflate(R.layout.me, container, false);
        ctx = getActivity().getApplicationContext();
        intent = getActivity().getIntent();

        personalInfo = (LinearLayout) vw.findViewById(R.id.personal_info);
        personalInfo.setOnClickListener(this);

        myAuction = (LinearLayout) vw.findViewById(R.id.myAuction);
        myAuction.setOnClickListener(this);

        myBid = (LinearLayout) vw.findViewById(R.id.myBid);
        myBid.setOnClickListener(this);

        myHistory = (LinearLayout) vw.findViewById(R.id.myHistory);
        myHistory.setOnClickListener(this);

        Button logoutBut = (Button) vw.findViewById(R.id.logoutButton);
        logoutBut.setOnClickListener(this);

        portrait = (ImageView) vw.findViewById(R.id.me_portrait);

        name = (TextView) vw.findViewById(R.id.me_UserName);
        name.setText(UserName);



        return vw;
    }

    @Override
    public void update(Observable observable, Object data) {

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
                Intent i = new Intent(getActivity().getBaseContext(), ModifyPersonalInfo.class);
                i.putExtra("api_key", ApiKey);
                i.putExtra("user_ID", UserID);
                getActivity().startActivityForResult(i, 3);
                break;
            case R.id.myBid:
                Intent intent1 = new Intent(ctx, TransactionInfo.class);
                intent1.putExtra("currentTransactionInfoBut", "biddingBut");
                intent1.putExtra("user_apiKey", ApiKey);
                intent1.putExtra("user_ID", UserID);
                startActivity(intent1);
                break;
            case R.id.myAuction:
                Intent intent2 = new Intent(ctx, TransactionInfo.class);
                intent2.putExtra("currentTransactionInfoBut", "sellingBut");
                intent2.putExtra("user_apiKey", ApiKey);
                intent2.putExtra("user_ID", UserID);
                startActivity(intent2);
                break;
            case R.id.myHistory:
                Intent intent3 = new Intent(ctx, TransactionInfo.class);
                intent3.putExtra("currentTransactionInfoBut", "bidHisBut");
                intent3.putExtra("user_apiKey", ApiKey);
                intent3.putExtra("user_ID", UserID);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }

    private void onLogoutPressed(){
        new AlertDialog.Builder(getActivity()).setTitle("Logout")
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

    @Override
    public void onPassAction(String a, String b, String c, String d) {
        UserName = a;
        Email = b;
        ApiKey = c;
        UserID = d;
    }

    private boolean updated = false;
    @Override
    public void onUpdateAction() {
        if(!updated) {
            updated = true;
            updatePortrait();
        }
    }

    @Override
    public void activityResultHandle(int requestCode, int resultCode, Intent data) {
        if (requestCode==3){
            Log.i("Debug::: ", "Return to personalInfoFragment");
            if(resultCode== Activity.RESULT_CANCELED)
                getActivity().setTitle("cancle");
            else if (resultCode== Activity.RESULT_OK) {
                updatePortrait();
            }
            else if (resultCode == Utility.RESULT_LOGOUT){
                getActivity().finish();

            }
        }
    }

    @Override
    public void onDoubleClick() {

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
                    getActivity().finish();
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
                URL url = new URL(Utility.serverUrlForPicture + "/portrait/" + fileName);
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

