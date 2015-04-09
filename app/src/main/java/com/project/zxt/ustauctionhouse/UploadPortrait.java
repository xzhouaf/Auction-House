package com.project.zxt.ustauctionhouse;

/**
 * Created by Paul on 2015/4/10.
 *
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

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
                createUserPortrait("cd8c812b03327d3557e2b531d7bd1fd4");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receivedCode;
    }

    protected void onPostExecute(Integer result){
        super.onPostExecute(result);
        if(result == 201){
            //Toast.makeText(getApplicationContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(getApplicationContext(),"Upload failed. Please try again!", Toast.LENGTH_SHORT).show();
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