package com.project.zxt.ustauctionhouse.PostItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project.zxt.ustauctionhouse.LoginRelated.LogIn;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.Utility.UploadImage;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Paul on 2015/4/12.
 *
 */
public class PostItem extends bottomMenuActivity implements View.OnClickListener, Observer{
    private static final String TAG = "PostItem";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private String image_file_name;
    private EditText time_limit, direct_buy_price, current_price;
    private Spinner condition_name, category_name;
    private TextView description, name;
    private ImageView itemImage;
    private Button confirm;
    private UploadImage imageUploader;

    public int getContentViewLayoutResId() { return R.layout.post_item; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");
        UserID = intent.getStringExtra("user_ID");

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

        image_file_name = "";
        itemImage = (ImageView) findViewById(R.id.PostitemImage);
        itemImage.setOnClickListener(this);

        condition_name = (Spinner) findViewById(R.id.PostItemConditionSel);
        category_name = (Spinner) findViewById(R.id.PostItemCategorySel);

        description = (TextView) findViewById(R.id.descriptionEditContent);
        name = (TextView) findViewById(R.id.itemNameInput);

        time_limit = (EditText) findViewById(R.id.limitTime);
        direct_buy_price = (EditText) findViewById(R.id.dirPrice);
        current_price = (EditText) findViewById(R.id.iniPrice);

        confirm = (Button) findViewById(R.id.confirm_post_button);
        confirm.setOnClickListener(this);

        new ConditionCategoryLoader(condition_name, category_name, this, true).loadConditionCategory();

    }

    private void modifyItemImage(){
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
        if (observable == imageUploader) {
            image_file_name = (String) data;
            Log.i(TAG, (String) data);
            imageUploader.deleteObserver(this);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.PostitemImage:
                imageUploader = new UploadImage(this, itemImage, "item");
                imageUploader.addObserver(this);
                modifyItemImage();
                break;
            case R.id.confirm_post_button:
                if(!validateInput()) return;
                new AsyncPostItem().execute();
                break;
            default:
                break;
        }
    }

    private boolean validateInput(){
        //TODO: Verify all the inputs by the user
        if (name.getText().toString().equals("")) {
            name.requestFocus();
            name.setError("You should input an item Name");
            return false;
        }
        if (description.getText().toString().equals("")) {
            description.requestFocus();
            description.setError("You should at least input one word of description!");
            return false;
        }

        if(time_limit.getText().toString().equals("")){
            time_limit.requestFocus();
            time_limit.setError("You should specify the time limit");
            return false;

        }

        if(current_price.getText().toString().equals("")){
            current_price.requestFocus();
            current_price.setError("An initial price should be specified!");
            return false;
        }
        if(image_file_name.toString().equals("")){
            Toast.makeText(this, "Upload image for your item!", Toast.LENGTH_SHORT).show();
            return false;
        }



        return true;
    }

    private class AsyncPostItem extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("name", name.getText().toString());
            NameValuePair pair2 = new BasicNameValuePair("description", description.getText().toString());
            NameValuePair pair3 = new BasicNameValuePair("condition_name", condition_name.getSelectedItem().toString());
            NameValuePair pair4 = new BasicNameValuePair("category_name", category_name.getSelectedItem().toString());
            NameValuePair pair5 = new BasicNameValuePair("time_limit", (Integer.parseInt(time_limit.getText().toString()))*3600+"");
            NameValuePair pair6 = new BasicNameValuePair("direct_buy_price", direct_buy_price.getText().toString());
            NameValuePair pair7 = new BasicNameValuePair("current_price", current_price.getText().toString());
            NameValuePair pair8 = new BasicNameValuePair("image_file_name", image_file_name);
            NameValuePair pair9 = new BasicNameValuePair("user_name", UserName);
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            pairList.add(pair4);
            pairList.add(pair5);
            pairList.add(pair6);
            pairList.add(pair7);
            pairList.add(pair8);
            pairList.add(pair9);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/postItem");
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


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}