package com.project.zxt.ustauctionhouse.ViewItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Unit;
import com.project.zxt.ustauctionhouse.Utility.Utility;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

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
    private ImageView image, bidNow, buyNow;
    private ImageLoader imageLoader;
    private TextView timeLeft, viewSeller;
    private TextView Description, ItemName, Price ,Category, Condition, DBPrice;
    private double cPrice;
    private UpdateTimeLeft timeUpdater;
    private boolean continueUpdate = true;
    private InputMethodManager imm;
    private String item_id, user_id, priceInput, ApiKey, UserID, dBuyPrice;
    private int intTimeLeft = 10000;
    private int status;

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
        DBPrice = (TextView) findViewById(R.id.ViewDBPrice);
        Category = (TextView) findViewById(R.id.ViewCategory);
        Condition = (TextView) findViewById(R.id.ViewCondition);
        bidNow = (ImageView) findViewById(R.id.bidNow);
        bidNow.setOnClickListener(this);
        buyNow = (ImageView) findViewById(R.id.buyNow);
        buyNow.setOnClickListener(this);
        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        ApiKey = intent.getStringExtra("API_key");
        UserID = intent.getStringExtra("user_ID");

        String imageFileURL = intent.getStringExtra(Utility.KEY_IMAGE);
        item_id = intent.getStringExtra((Utility.KEY_ID));
        imageLoader = new ImageLoader(ctx);
        imageLoader.DisplayImage(imageFileURL, image);

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
                break;
            case R.id.buyNow:
                onBuyNowClick();
                break;
            default:
                break;
        }
    }

    private void onBuyNowClick(){

        if (user_id.equals(UserID)){
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("You can't buy your own item!")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                               java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                               field.setAccessible(true);
                               field.set(dialog,true);
                               } catch (Exception e) {
                                e.printStackTrace();
                               }

                        }
                    })
                    .show();
            return;

        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("You would like to pay $"+dBuyPrice+" to buy this item ?")
                .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new AsyncDirectBuy().execute();
                        try {
                            java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        finish();

                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            java.lang.reflect.Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                })
                .show();





    }

    private void onBidNowClick(){
        Log.i("Debugggg ", "user_id" + user_id + "UserID" + UserID);
        final EditText userInputPrice = new EditText(this);
        userInputPrice.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        if (user_id.equals(UserID)){
            new AlertDialog.Builder(this)
                    .setTitle("Information")
                    .setMessage("You can't buy your own item!")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                        }
                    })
                    .show();
            return;

        }
        AlertDialog a = new AlertDialog.Builder(this)
                .setTitle("Input your bid price here")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(userInputPrice)
                .setPositiveButton("Bid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                        if(confirmBid(userInputPrice.getText().toString())){
                            priceInput = userInputPrice.getText().toString();
                            new AsyncPlaceBid().execute();
                            new AsyncGetSingleItem().execute();




                        }
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

    private boolean confirmBid(String input){
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
            return false;
        }

        if(Double.valueOf(input)<findMinBidPrice()){

            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("You must input a price larger than $"+findMinBidPrice())
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBidNowClick();
                        }
                    })
                    .show();
            return false;

        }
        return true;
    }

    private double findMinBidPrice(){


        if (cPrice/20>1){
            return formatDouble((int)cPrice/20+cPrice);
        }
        return formatDouble(cPrice+1);
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
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
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
            cPrice = unitList.get(0).currentPrice;
            dBuyPrice = unitList.get(0).directBuyPrice+"";
            if (Double.valueOf(dBuyPrice)>0.001){
                DBPrice.setText("$ "+unitList.get(0).directBuyPrice);
            }
            else{
                DBPrice.setText("N/A");

            }
            Category.setText(unitList.get(0).categoryName);
            intTimeLeft = Integer.valueOf(unitList.get(0).timeLeft);
            user_id =unitList.get(0).userID+"";
            status = unitList.get(0).status;
            if (status == 1){
                bidNow.setOnClickListener(null);
                buyNow.setOnClickListener(null);
                bidNow.setImageResource(R.drawable.bid_now_grey);
                buyNow.setImageResource(R.drawable.buy_now_grey);

            }

            else{
                bidNow.setImageResource(R.drawable.bid_now);
                if (Double.valueOf(dBuyPrice)<0.001){
                    buyNow.setImageResource(R.drawable.buy_now_grey);
                    buyNow.setOnClickListener(null);

                }
                else{
                    buyNow.setImageResource(R.drawable.buy_now);
                }

            }


        }
    }

    private class AsyncPlaceBid extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {


            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("item_id", item_id);
            NameValuePair pair2 = new BasicNameValuePair("bid_price", priceInput);


            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/placeBid");
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

    private class AsyncDirectBuy extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {


            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("item_id", item_id);
            NameValuePair pair2 = new BasicNameValuePair("buy_price", dBuyPrice);


            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/directBuy");
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