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
    private EditText time_limit, direct_buy_price, current_price;
    private Spinner condition_name, category_name;
    private TextView description, name;
    private ImageView itemImage;
    private Button confirm;

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

        condition_name = (Spinner) findViewById(R.id.PostItemConditionSel);
        category_name = (Spinner) findViewById(R.id.PostItemCategorySel);

        description = (TextView) findViewById(R.id.descriptionEditContent);
        name = (TextView) findViewById(R.id.itemNameInput);

        time_limit = (EditText) findViewById(R.id.limitTime);
        direct_buy_price = (EditText) findViewById(R.id.dirPrice);
        current_price = (EditText) findViewById(R.id.iniPrice);

        confirm = (Button) findViewById(R.id.confirm_post_button);
        confirm.setOnClickListener(this);

        new AsyncGetConditionCategory().execute();

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
            case R.id.PostitemImage:
                modifyItemImage();
                break;
            case R.id.confirm_post_button:
                Log.i(TAG, condition_name.getSelectedItem().toString());
                new AsyncPostItem().execute();
                break;
            default:
                break;
        }
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
            NameValuePair pair5 = new BasicNameValuePair("time_limit", time_limit.getText().toString());
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


    private class AsyncGetConditionCategory extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            // 使用GET方法发送请求,需要把参数加在URL后面，用？连接，参数之间用&分隔
            String url1 = Utility.serverUrl + "/conditionAndCategory";
            // 生成请求对象
            HttpGet httpGet = new HttpGet(url1);
            HttpClient httpClient = new DefaultHttpClient();
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
                if(result.getString("error").equals("true")){
                    Log.i("Update Failed!!!", "cannot update condition category");
                }else{
                    Log.i("Update Result:", result.toString());
                    jsonAnalysis(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void jsonAnalysis(JSONObject obj) throws JSONException {
            if(!obj.isNull("condition")){
                JSONArray conditionArray = obj.getJSONArray("condition");
                String conditionNewArray[] = new String[conditionArray.length()];
                for(int i = 0; i < conditionArray.length(); i++){
                    conditionNewArray[i] = (String)((JSONObject)conditionArray.get(i)).get("condition_name");
                    //Log.i("Important: ", conditionNewArray[i]);
                }
                ArrayAdapter adapter=new ArrayAdapter(getApplicationContext(),R.layout.login_list_item,conditionNewArray);
                condition_name.setAdapter(adapter);
            }
            if(!obj.isNull("category")){
                JSONArray categoryArray = obj.getJSONArray("category");
                String categoryNewArray[] = new String[categoryArray.length()];
                for(int i = 0; i < categoryArray.length(); i++){
                    categoryNewArray[i] = (String)((JSONObject)categoryArray.get(i)).get("category_name");
                    //Log.i("Important: ", categoryNewArray[i]);
                }
                ArrayAdapter adapter=new ArrayAdapter(getApplicationContext(),R.layout.login_list_item,categoryNewArray);
                category_name.setAdapter(adapter);
            }
        }
    }


}