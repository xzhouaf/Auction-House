package com.project.zxt.ustauctionhouse;

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


/**
 * Created by Paul on 2015/4/10.
 *
 */
public class ModifyPersonalInfo extends Activity implements View.OnClickListener {

    private final String TAG = "ModifyPersonalInfo";
    private Context ctx;
    ImageButton portraitMod;
    Intent intent;
    private String ApiKey;

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
                Uri uri = data.getData();
                Log.i("uri", uri.toString());
                startPhotoZoom(uri);
                break;
            case 2:
                if(data != null){
                    uploadPortraitToServer(data);
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

    private void uploadPortraitToServer(Intent picdata){
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            portraitMod.setImageBitmap(photo);
            new UploadPortrait().execute(photo);
        }
    }

    public class UploadPortrait extends AsyncTask<Bitmap, Void, Integer> {

        private String TAG = "UploadPortrait";
        private String imageFileName = "";
        private String currentTime = "";

        protected void onPreExecute(){
            super.onPreExecute();
            Calendar c = Calendar.getInstance();
            currentTime = c.getTimeInMillis()+"";
            imageFileName = currentTime + ".bmp";
        }

        @Override
        protected Integer doInBackground(Bitmap... params) {
            String imagePath = savePic(params[0], currentTime);
            Integer receivedCode = 0;
            try {
                receivedCode = uploadByCommonPost(imagePath);
                if(receivedCode == 201) {
                    createUserPortrait(ApiKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receivedCode;
        }

        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result == 201){
                Toast.makeText(ctx, "Portrait updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                //intent.putExtra("fileName", imageFileName);
                setResult(RESULT_OK, intent);
                //parent.putExtra("portraitFile", imageFileName);
            }else{
                Toast.makeText(ctx,"Update failed. Please try again!", Toast.LENGTH_SHORT).show();
                //imageFileName = "";
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
            URL url = new URL(Utility.serverUrl + "/uploadPortrait");
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

        private void createUserPortrait(String api_key){
            NameValuePair pair2 = new BasicNameValuePair("file_name", imageFileName);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair2);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/userPortrait");
                httpPost.addHeader("Authorization", api_key);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);
                JSONObject obj = Utility.response2obj(response);
                Log.i(TAG, obj.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
                URL url = new URL(Utility.serverUrl + "/portrait/" + fileName);
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
                portraitMod.setImageBitmap(result);
            }else{
                portraitMod.setImageDrawable(getResources().getDrawable(R.drawable.hhh));
            }
        }
    }


}