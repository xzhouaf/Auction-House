package com.project.zxt.ustauctionhouse.PersonalInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.LoginRelated.LogIn;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.UploadImage;
import com.project.zxt.ustauctionhouse.Utility.Utility;

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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by Paul on 2015/4/10.
 *
 */
public class ModifyPersonalInfo extends Activity implements View.OnClickListener, Observer {

    private final String TAG = "ModifyPersonalInfo";
    private Context ctx;
    ImageView portraitMod, update, cancel;
    EditText user_name,phone_, old_pass, new_pass, new_pass2;
    String userName, Phone, oldPass, newPass, newPass2;
    Intent intent;
    private String ApiKey;
    private ImageLoader imageLoader;
    private InputMethodManager imm;
    private UploadImage imageUploader;
    private boolean success = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_modify);
        Log.i(TAG, "Activity: onCreate()");

        user_name = (EditText) findViewById(R.id.userNameMod);
        user_name.setText(null);
        phone_ = (EditText) findViewById(R.id.phoneMod);
        phone_.setText(null);
        old_pass = (EditText) findViewById(R.id.oPassField);
        old_pass.setText(null);
        new_pass = (EditText) findViewById(R.id.nPassField);
        new_pass.setText(null);
        new_pass2 = (EditText) findViewById(R.id.cPassField);
        new_pass2.setText(null);

        update = (ImageView) findViewById(R.id.InfoModUpdate);
        update.setOnClickListener(this);
        cancel = (ImageView) findViewById(R.id.InfoModCancel);
        cancel.setOnClickListener(this);

        ctx = getApplicationContext();
        //parent = (Intent)intent.getExtras().get("parent");
        intent = this.getIntent();
        ApiKey = intent.getStringExtra("api_key");
        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        portraitMod = (ImageView) findViewById(R.id.portraitMod);
        portraitMod.setOnClickListener(this);

        new AsyncDownloadPortrait().execute();
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.portraitMod:
                imageUploader = new UploadImage(this, portraitMod, ApiKey);
                imageUploader.addObserver(this);
                modifyPortrait();
                break;

            case R.id.InfoModUpdate:
                onUpdateClick();
                break;

            case R.id.InfoModCancel:
                finish();
                break;

            default:
                break;
        }
    }

    private void onUpdateClick(){
        newPass = new_pass.getText().toString();
        newPass2 = new_pass2.getText().toString();
        oldPass = old_pass.getText().toString();
        Phone = phone_.getText().toString();
        userName = user_name.getText().toString();

        if(!newPass.equals(newPass2)){

            Toast.makeText(ctx, "New password and confirm password don't match!", Toast.LENGTH_SHORT).show();

        }
        else {

            if ((!newPass.equals(null))&&(!oldPass.equals(null))) {
                new AsyncLogout().execute();

            }
            else{
                new AsyncUpdate().execute();
                if (success) {
                    finish();
                }
            }


        }



    }

    private void modifyPortrait(){
        final CharSequence[] items = { "Camera", "Gallery" };
        new AlertDialog.Builder(this).setTitle("Select Source")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                intent.setAction(Intent.ACTION_PICK);
                                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            } else {
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                            }
                            startActivityForResult(intent, 1);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 1);
                        }
                    }
                }).create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(data != null) {
                    Uri uri = data.getData();
                    Log.i("uri", uri.toString());
                    imageUploader.startPhotoZoom(uri);
                }
                break;
            case 2:
                if(data != null){
                    imageUploader.uploadItemImageToServer(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void update(Observable observable, Object data) {
        if(observable == imageUploader){
            Log.i(TAG, (String)data);
            imageUploader.deleteObserver(this);
            if(data != null) {
                setResult(RESULT_OK, intent);
            }
        }
    }

    private class AsyncDownloadPortrait extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

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
            return fileName;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result != null) {
                String imageFileURL = Utility.serverUrlForPicture + "/portrait/" + result;
                imageLoader = new ImageLoader(ctx);
                imageLoader.DisplayImage(imageFileURL, portraitMod);
            }else{
                portraitMod.setImageDrawable(getResources().getDrawable(R.drawable.hhh));
            }
        }
    }

    private class AsyncUpdate extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {


            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("user_name", userName);
            NameValuePair pair2 = new BasicNameValuePair("phone_number", Phone);
            NameValuePair pair3 = new BasicNameValuePair("old_password", oldPass);
            NameValuePair pair4 = new BasicNameValuePair("new_password", newPass);



            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            pairList.add(pair4);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/updateProfile");
                httpPost.addHeader("Authorization", ApiKey);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);
                obj = Utility.response2obj(response);
            } catch (Exception e) {
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
                if(result.getString("error").equals("true")){

                }else{

                    success = true;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finish();
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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    SharedPreferences.Editor prefed = prefs.edit();
                    prefed.putString("loggedInA", null);
                    prefed.putString("loggedInB", null);
                    prefed.putString("loggedInC", null);
                    prefed.putString("loggedInD", null);
                    prefed.apply();
                    Intent i = new Intent(ctx, LogIn.class);
                    startActivity(i);
                    new AsyncUpdate().execute();
                    getParent().finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}