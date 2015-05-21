/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.PostItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.Utility.UploadImage;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnection;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnectionHandler;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketException;
import com.project.zxt.ustauctionhouse.bottomMenu.BottomMenuHome;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class PostItemFragment extends Fragment implements View.OnClickListener, Observer, BottomMenuHome.OnPassParamListener {

    private static final String TAG = "PostItem";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private String image_file_name;
    private EditText time_limit, direct_buy_price, current_price;
    private Spinner condition_name, category_name, post_mode;
    private TextView description, name, timeLimitTag, hourTag;
    private ImageView itemImage;
    private Button confirm;
    private UploadImage imageUploader;

    public static String wsUrl = "ws://gaozihou.no-ip.org:7272";
    public WebSocketConnection wsC = new WebSocketConnection();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "Fragment View: onCreate()");

        View vw = inflater.inflate(R.layout.post_item, container, false);
        intent = getActivity().getIntent();
        ctx = getActivity().getApplicationContext();

        image_file_name = "";
        itemImage = (ImageView) vw.findViewById(R.id.PostitemImage);
        itemImage.setOnClickListener(this);

        timeLimitTag = (TextView)vw.findViewById(R.id.timeLimitTag);
        hourTag = (TextView) vw.findViewById(R.id.hrsTag);

        condition_name = (Spinner) vw.findViewById(R.id.PostItemConditionSel);
        category_name = (Spinner) vw.findViewById(R.id.PostItemCategorySel);
        post_mode = (Spinner) vw.findViewById(R.id.post_mode);

        description = (TextView) vw.findViewById(R.id.descriptionEditContent);
        name = (TextView) vw.findViewById(R.id.itemNameInput);

        time_limit = (EditText) vw.findViewById(R.id.limitTime);
        direct_buy_price = (EditText) vw.findViewById(R.id.dirPrice);
        current_price = (EditText) vw.findViewById(R.id.iniPrice);

        confirm = (Button) vw.findViewById(R.id.confirm_post_button);
        confirm.setOnClickListener(this);

        String conditionNewArray[] = {"Regular", "Live"};
        ArrayAdapter adapter=new ArrayAdapter(ctx,R.layout.login_list_item,conditionNewArray);
        post_mode.setAdapter(adapter);
        post_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Debug::: ", "Hihi: " + position);
                switch (position) {
                    case 0:
                        condition_name.setEnabled(true);
                        category_name.setEnabled(true);
                        direct_buy_price.setEnabled(true);
                        timeLimitTag.setText("time limit: ");
                        hourTag.setText("hours");
                        break;
                    case 1:
                        condition_name.setEnabled(false);
                        category_name.setEnabled(false);
                        direct_buy_price.setEnabled(false);
                        timeLimitTag.setText("start time: ");
                        hourTag.setText("minutes later");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        return vw;
    }

    private void modifyItemImage(){
        final CharSequence[] items = { "Camera", "Gallery" };
        new AlertDialog.Builder(getActivity()).setTitle("Select Source")
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
                            getActivity().startActivityForResult(intent, 1);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            getActivity().startActivityForResult(intent, 1);
                        }
                    }
                }).create().show();
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
                imageUploader = new UploadImage(getActivity(), itemImage, "item");
                imageUploader.addObserver(this);
                modifyItemImage();
                break;
            case R.id.confirm_post_button:
                switch (post_mode.getSelectedItem().toString()){
                    case "Regular":
                        if(!validateInput()) return;
                        new AsyncPostItem().execute();
                        break;
                    case "Live":
                        if(!validateLiveInput()) return;
                        wsStart();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void wsStart()
    {
        /*
            BasicNameValuePair pair1 = new BasicNameValuePair("Authorization", ApiKey);
            List<BasicNameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            */
        try {
            wsC.connect( wsUrl, null, new WebSocketConnectionHandler()
            {
                @Override
                public void onOpen()
                {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", "add");
                        obj.put("Authorization", ApiKey);
                        obj.put("seller_name", UserName);
                        obj.put("image", image_file_name);
                        obj.put("description", description.getText().toString());
                        obj.put("room_id", 2);
                        obj.put("later", Integer.valueOf(time_limit.getText().toString())*60);
                        obj.put("name", name.getText().toString());
                        obj.put("initial_price", current_price.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    wsC.sendTextMessage(obj.toString());
                }

                @Override
                public void onTextMessage( String payload ){

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(payload);
                        switch(obj.getString("type")){
                            case "ping":
                                JSONObject obj_send = new JSONObject();
                                try {
                                    obj_send.put("type", "pong");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                wsC.sendTextMessage(obj_send.toString());
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose( int code, String reason ){
                    Toast.makeText(ctx, "Item posted to live!", Toast.LENGTH_SHORT).show();
                }
            } );
        } catch ( WebSocketException e ) {
            e.printStackTrace();
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
        if(Float.valueOf(current_price.getText().toString())<0.5) {
            current_price.requestFocus();
            current_price.setError("Initial price should be at least $ 0.5!");
            return false;
        }
        if(image_file_name.toString().equals("")){
            Toast.makeText(getActivity(), "Upload image for your item!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateLiveInput(){
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
            time_limit.setError("You should specify the start time!");
            return false;

        }


        if(current_price.getText().toString().equals("")){
            current_price.requestFocus();
            current_price.setError("An initial price should be specified!");
            return false;
        }

        if(Float.valueOf(current_price.getText().toString())<0.5){
            current_price.requestFocus();
            current_price.setError("Initial price should be at least $ 0.5!");
            return false;

        }

        if(image_file_name.toString().equals("")){
            Toast.makeText(getActivity(), "Upload image for your item!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onPassAction(String a, String b, String c, String d) {
        UserName = a;
        Email = b;
        ApiKey = c;
        UserID = d;
    }

    boolean updated = false;
    @Override
    public void onUpdateAction() {
        if(!updated) {
            updated = true;
            new ConditionCategoryLoader(condition_name, category_name, getActivity(), true).loadConditionCategory();
        }
    }

    @Override
    public void activityResultHandle(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Success2, " + requestCode);
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
    }

    @Override
    public void onDoubleClick() {

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
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
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
