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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Paul on 2015/4/12.
 *
 */
public class PostItem extends bottomMenuActivity implements View.OnClickListener{
    private static final String TAG = "PostItem";
    private String UserName, Email, ApiKey, CreatedAt;
    private Intent intent;
    private Context ctx;
    private String image_file_name;
    private EditText name, description, time_limit,
                    direct_buy_price, current_price;
    private Spinner condition, category_name;
    private ImageView itemImage;

    public int getContentViewLayoutResId() { return R.layout.post_item; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

        image_file_name = "";
        itemImage = (ImageView) findViewById(R.id.PostitemImage);
        itemImage.setOnClickListener(this);

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
                    startPhotoZoom(uri);
                }
                break;
            case 2:
                if(data != null){
                    uploadItemImageToServer(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri) {
        /*
         * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
         * yourself_sdk_path/docs/reference/android/content/Intent.html
         * 直接在里面Ctrl+F搜：CROP ，之前小马没仔细看过，其实安卓系统早已经有自带图片裁剪功能,
         * 是直接调本地库的，小马不懂C C++  这个不做详细了解去了，有轮子就用轮子，不再研究轮子是怎么
         * 制做的了...吼吼
         */
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    private void uploadItemImageToServer(Intent picdata){
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            itemImage.setImageBitmap(photo);
            new UploadItemImage().execute(photo);
        }
    }

    public class UploadItemImage extends AsyncTask<Bitmap, Void, Integer> {

        private String TAG = "UploadItemImage";
        private String tempImageFileName = "";
        private String currentTime = "";

        protected void onPreExecute(){
            super.onPreExecute();
            Calendar c = Calendar.getInstance();
            currentTime = c.getTimeInMillis()+"";
            tempImageFileName = currentTime + ".bmp";
        }

        @Override
        protected Integer doInBackground(Bitmap... params) {
            String imagePath = savePic(params[0], currentTime);
            Integer receivedCode = 0;
            try {
                receivedCode = uploadByCommonPost(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receivedCode;
        }

        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result == 201){
                Toast.makeText(ctx, "Item image updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                image_file_name = tempImageFileName;
                setResult(RESULT_OK, intent);
            }else{
                Toast.makeText(ctx,"Upload item image failed. Please try again!", Toast.LENGTH_SHORT).show();
                image_file_name = "";
            }
        }

        private String savePic(Bitmap b, String time) {
            String path = "";
            FileOutputStream fos = null;
            try {
                path = Utility.DATABASE_PATH + "/temp/" + time + ".bmp";
                File f = new File(path);
                if (f.exists()) {
                    f.delete();
                }
                fos = new FileOutputStream(f);
                Log.i(TAG,"strFileName 1= " + f.getPath());
                if (null != fos) {
                    b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                    Log.i(TAG,"save pic OK!");
                }
            } catch (FileNotFoundException e) {
                Log.i(TAG,"FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(TAG,"IOException");
                e.printStackTrace();
            }
            return path;
        }

        private int uploadByCommonPost(String path) throws IOException {

            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            URL url = new URL(Utility.serverUrl + "/upload");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(
                    httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
                    + path.substring(path.lastIndexOf("/") + 1) + "\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            int response = httpURLConnection.getResponseCode();
            dos.close();

            new File(path).delete();
            return response;
        }
    }



    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }




}