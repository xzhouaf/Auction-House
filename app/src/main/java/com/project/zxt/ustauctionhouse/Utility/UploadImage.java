/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Observable;


public class UploadImage extends Observable{

    private static final String TAG = "Upload Image";
    private String imageFileName;
    private Activity parent;
    private ImageView tempImage;
    private String type;

    public UploadImage(Activity a, ImageView i, String t){
        parent = a;
        tempImage = i;
        type = t;
    }

    public void startPhotoZoom(Uri uri) {
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
        parent.startActivityForResult(intent, 2);
    }

    public void uploadItemImageToServer(Intent picdata){
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            tempImage.setImageBitmap(photo);
            new UploadItemImage().execute(photo);
        }
    }

    private class UploadItemImage extends AsyncTask<Bitmap, Void, Integer> {

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
                if(receivedCode == 201 && (!type.equals("item"))){
                    createUserPortrait(type);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receivedCode;
        }

        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result == 201){
                if(type.equals("item")) {
                    Toast.makeText(parent, "Item image updated successfully!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(parent, "Portrait updated successfully!", Toast.LENGTH_SHORT).show();
                }
                imageFileName = tempImageFileName;
            }else{
                Toast.makeText(parent,"Upload image failed. Please try again!", Toast.LENGTH_SHORT).show();
                imageFileName = "";
            }
            setChanged();
            notifyObservers(imageFileName);
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
            URL url;
            if(type.equals("item")) {
                url = new URL(Utility.serverUrl + "/upload");
            }else{
                url = new URL(Utility.serverUrl + "/uploadPortrait");
            }
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
            NameValuePair pair2 = new BasicNameValuePair("file_name", tempImageFileName);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair2);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
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


}
