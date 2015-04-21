package com.project.zxt.ustauctionhouse.ViewItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.project.zxt.ustauctionhouse.GCM.RegisterApp;
import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.LoginRelated.Register;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Unit;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.NewItem;

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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.project.zxt.ustauctionhouse.Utility.Utility.formatDouble;
import static com.project.zxt.ustauctionhouse.Utility.Utility.secondsToTime;

/**
 * Created by Paul on 2015/4/14.
 *
 */
public class ViewItem extends Activity implements View.OnClickListener {

    private static final String TAG = "View Item";
    private Intent intent;
    private Context ctx;
    private ImageView image, bidNow;
    private ImageLoader imageLoader;
    private TextView timeLeft, viewSeller;
    private TextView Description, ItemName, Price ,Category, Condition;
    private UpdateTimeLeft timeUpdater;
    private boolean continueUpdate = true;
    private InputMethodManager imm;
    private String item_id,user_id, UserID;
    private int intTimeLeft = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_item);
        Log.i(TAG, "Activity: onCreate()");

        intent = getIntent();
        ctx = getApplicationContext();

        image = (ImageView) findViewById(R.id.ViewItemImage);
        timeLeft = (TextView) findViewById(R.id.ViewTimeLeft);
        viewSeller = (TextView) findViewById(R.id.ViewSeller);
        viewSeller.setOnClickListener(this);
        Description = (TextView) findViewById(R.id.ViewDescription);
        ItemName = (TextView) findViewById(R.id.ViewItemName);
        Price = (TextView) findViewById(R.id.ViewPrice);
        Category = (TextView) findViewById(R.id.ViewCategory);
        Condition = (TextView) findViewById(R.id.ViewCondition);
        bidNow = (ImageView) findViewById(R.id.bidNow);
        bidNow.setOnClickListener(this);
        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        String imageFileURL = intent.getStringExtra(Utility.KEY_IMAGE);
        item_id = intent.getStringExtra((Utility.KEY_ID));
        imageLoader = new ImageLoader(ctx);
        imageLoader.DisplayImage(imageFileURL, image);
        UserID = intent.getStringExtra("user_ID");

        new AsyncGetSingleItem().execute();

        timeUpdater = new UpdateTimeLeft();
        timeUpdater.executeOnExecutor(Executors.newCachedThreadPool());

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        continueUpdate = false;
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bidNow:
                onBidNowClick();
                break;
            case R.id.ViewSeller:
                Intent intent = new Intent(ctx, ViewSeller.class);
                intent.putExtra("USER_KEY",user_id);
                intent.putExtra("SELLER_NAME",viewSeller.getText());
                startActivity(intent);
            default:
                break;
        }
    }

    private void onBidNowClick(){
        final EditText userInputPrice = new EditText(this);
        userInputPrice.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        AlertDialog a = new AlertDialog.Builder(this)
                .setTitle("Input your bid price here")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(userInputPrice)
                .setPositiveButton("Bid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                        confirmBid(userInputPrice.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                    }
                })
                .show();

        Timer timer = new Timer();
        timer.schedule(
                new TimerTask(){
                    @Override
                    public void run(){
                        imm.showSoftInput(userInputPrice, InputMethodManager.HIDE_NOT_ALWAYS);
                        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }, 50);
    }

    private void confirmBid(String input){
        if(!validateInputPrice(input)){
            new AlertDialog.Builder(this)
                    .setTitle("Information")
                    .setMessage("You must input a valid price")
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBidNowClick();
                        }
                    })
                    .show();
            return;
        }
    }

    private boolean validateInputPrice(String in){
        //TODO: validate input price
        if(in.equals("")) return false;
        return true;
    }

    private class UpdateTimeLeft extends AsyncTask<Object, String, Boolean> {

        private int counter = 0;

        @Override
        protected void onProgressUpdate(String... values) {
            timeLeft.setText(secondsToTime(intTimeLeft - counter));
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            while(continueUpdate) {
                counter++;
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Stopped! ", "");
            return null;
        }
    }

    private class AsyncGetSingleItem extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            NameValuePair pair1 = new BasicNameValuePair("task_id", item_id);

            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = null;

                    httpPost = new HttpPost(Utility.serverUrl + "/getSingleItem");


                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);

                String line = "";
                HttpEntity receivedEntity = response.getEntity();
                InputStream receivedStream = receivedEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        receivedStream));
                while (null != (line = reader.readLine())) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result){
            List<Unit> unitList = null;
            try {
                unitList = Utility.string2unit(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ItemName.setText(unitList.get(0).name);
            Condition.setText(unitList.get(0).conditionName);
            Description.setText(unitList.get(0).description);
            viewSeller.setText(unitList.get(0).userName);
            Price.setText("$ "+unitList.get(0).currentPrice);
            Category.setText(unitList.get(0).categoryName);
            intTimeLeft = Integer.valueOf(unitList.get(0).timeLeft);
            user_id =unitList.get(0).userID+"";





        }
    }


}