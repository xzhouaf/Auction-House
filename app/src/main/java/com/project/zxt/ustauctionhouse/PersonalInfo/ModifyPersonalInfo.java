package com.project.zxt.ustauctionhouse.PersonalInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
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
    ImageButton portraitMod;
    Intent intent;
    private String ApiKey;
    private ImageLoader imageLoader;
    private UploadImage imageUploader;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_modify);
        Log.i(TAG, "Activity: onCreate()");

        ctx = getApplicationContext();
        //parent = (Intent)intent.getExtras().get("parent");
        intent = this.getIntent();
        ApiKey = intent.getStringExtra("api_key");

        portraitMod = (ImageButton) findViewById(R.id.portraitMod);
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

            default:
                break;
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
                String imageFileURL = Utility.serverUrl + "/portrait/" + result;
                imageLoader = new ImageLoader(ctx);
                imageLoader.DisplayImage(imageFileURL, portraitMod);
            }else{
                portraitMod.setImageDrawable(getResources().getDrawable(R.drawable.hhh));
            }
        }
    }


}