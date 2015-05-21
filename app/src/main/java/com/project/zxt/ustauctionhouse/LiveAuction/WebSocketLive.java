/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */


package com.project.zxt.ustauctionhouse.LiveAuction;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.LiveUnit;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnection;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnectionHandler;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import static com.project.zxt.ustauctionhouse.Utility.Utility.secondsToTime;


public class WebSocketLive extends Activity implements View.OnClickListener {

    private final String TAG = "WebSocketLive";
    private Context ctx;
    private Intent intent;
    private TextView timeText, clientView, priceView, statusTimeText, countView, statusView;
    private TextView bidBut;
    private String ApiKey, UserID, UserName, dialogTitle, dialogSeller, dialogDescription;
    private int seller_id, status, time_left;
    private String roomID, image_name, clientList = "";
    private ListView bidList;
    private LiveBidAdapter adapter;
    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
    private ImageLoader imageLoader;
    private ImageView goodImage;
    private Bitmap imageBitmap;
    private float current_price = 0;
    private AlertDialog inputDialog;
    public WebSocketConnection wsC = new WebSocketConnection();
    private long curr_time;
    private boolean continueUpdate = true, continueUpdateAnim = true,
            needToastForLost = true, isReconnection = false;
    private UpdateTimeLeft timeUpdater;
    private UpdateAnimation animUpdater;
    private int currAnimState = 0;

    public void toastLog( String s )
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_bid);
        ctx = getApplicationContext();
        intent = getIntent();

        ApiKey = intent.getStringExtra("user_apiKey");
        UserID = intent.getStringExtra("user_ID");
        UserName = intent.getStringExtra("user_name");
        roomID = intent.getStringExtra("room_id");
        image_name = intent.getStringExtra("image");

        timeText = (TextView) findViewById(R.id.livebid_curr_time);
        goodImage = (ImageView) findViewById(R.id.livebid_image);
        goodImage.setOnClickListener(this);
        bidBut = (TextView) findViewById(R.id.livebid_bid_but);
        bidBut.setOnClickListener(this);

        clientView = (TextView) findViewById(R.id.livebid_user_list);
        clientList = UserName;
        priceView = (TextView) findViewById(R.id.livebid_price);
        statusTimeText = (TextView) findViewById(R.id.livebid_status_time);
        countView = (TextView) findViewById(R.id.livebid_countdown);
        statusView = (TextView) findViewById(R.id.livebid_status_text);

        bidList = (ListView) findViewById(R.id.livebid_listview);
        adapter = new LiveBidAdapter(this, dataToDisplay);
        bidList.setAdapter(adapter);

        new AsyncDownloadImage().execute();

        wsStart();

    }

    private void wsStart()
    {
        /*
            BasicNameValuePair pair1 = new BasicNameValuePair("Authorization", ApiKey);
            List<BasicNameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            */
        try {
            wsC.connect( Utility.wsUrl, null, new WebSocketConnectionHandler()
            {
                @Override
                public void onOpen()
                {
                    if(isReconnection) {
                        toastLog("Reconnected");
                    }

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", "login");
                        obj.put("Authorization", ApiKey);
                        obj.put("user_id", UserID);
                        obj.put("client_name", UserName);
                        obj.put("room_id", Integer.valueOf(roomID));
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
                            case "login":
                                resetClientView(obj.getString("client_list"));
                                if(obj.getString("user_id").equals(UserID) && !isReconnection) {
                                    timeText.setText(obj.getString("time"));
                                    current_price = Float.valueOf(obj.getString("price"));
                                    priceView.setText("$" + current_price);
                                    dialogTitle = obj.getString("name");
                                    dialogSeller = obj.getString("seller_name");
                                    dialogDescription = obj.getString("description");
                                    seller_id = Integer.valueOf(obj.getString("seller_id"));
                                    status = obj.getInt("status");

                                    Date curr_date = Utility.format.parse(obj.getString("time"));
                                    curr_time = curr_date.getTime();
                                    time_left = Integer.valueOf(obj.getString("time_left"));

                                    Date left = new Date(time_left * 1000);
                                    countView.setText(Utility.timeLeftFormat.format(left));

                                    changeStatusDisplay(obj.getString("start_time"));
                                    timeUpdater = new UpdateTimeLeft();
                                    timeUpdater.executeOnExecutor(Executors.newCachedThreadPool());

                                    resetBidList(obj.getString("bid_list"));
                                }
                                break;
                            case "logout":
                                resetClientView(obj.getString("client_list"));
                                break;
                            case "say":
                                current_price = Float.valueOf(obj.getString("price"));
                                priceView.setText("$" + current_price);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Utility.KEY_CURRENT_PRICE, "$" + current_price);
                                map.put(Utility.KEY_NAME, obj.getString("from_client_name"));
                                map.put("time", obj.getString("time"));
                                Collections.reverse(dataToDisplay);
                                dataToDisplay.add(map);
                                Collections.reverse(dataToDisplay);
                                adapter.updateView(dataToDisplay);
                                //Collections.reverse(dataToDisplay);
                                break;
                            case "start":
                                status = 2;
                                changeStatusDisplay("");
                                toastLog("Bid start now!");
                                break;
                            case "end":
                                if(status != 3) {
                                    status = 3;
                                    changeStatusDisplay("");
                                    toastLog("Bid end now!");
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose( int code, String reason ) throws InterruptedException {
                    if(needToastForLost) {
                        toastLog("Connection lost");
                        //TODO Reconnection
                        Thread.sleep(3000);
                        wsStart();
                        isReconnection = true;
                    }
                }
            } );
        } catch ( WebSocketException e ) {
            e.printStackTrace();
        }
    }

    private void changeStatusDisplay(String start_time){
        switch (status){
            case 1:
                statusView.setText("Status: Not started yet");
                break;
            case 2:
                statusView.setText("Status: Bidding now!");
                animUpdater = new UpdateAnimation();
                animUpdater.executeOnExecutor(Executors.newCachedThreadPool());
                break;
            case 3:
                statusView.setText("Status: Finished");
                countView.setText("Finished");
                continueUpdateAnim = false;
                break;
            default:
                break;
        }
        if(!start_time.equals("")) {
            statusTimeText.setText("Start at " + start_time);
        }
    }

    private class UpdateTimeLeft extends AsyncTask<Object, String, Boolean> {

        @Override
        protected void onProgressUpdate(String... values) {
            curr_time += 1000;
            Date date = new Date(curr_time);
            timeText.setText(Utility.format.format(date));
            if(status == 2 && time_left != 0){
                time_left--;
                Date left = new Date(time_left*1000);
                countView.setText(Utility.timeLeftFormat.format(left));
            }else if(status == 2){
                status = 3;
                changeStatusDisplay("");
                toastLog("Bid end now!");
            }
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            while(continueUpdate) {
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

    private class UpdateAnimation extends AsyncTask<Object, String, Boolean> {

        @Override
        protected void onProgressUpdate(String... values) {
            if(status == 2){
                if(currAnimState == 1){
                    currAnimState = 0;
                    bidBut.setBackgroundColor(0xff669bff);
                }else{
                    currAnimState = 1;
                    bidBut.setBackgroundColor(0xFFFF5A26);
                }
            }
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            while(continueUpdateAnim) {
                publishProgress();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Stopped! ", "");
            return false;
        }

        protected void onPostExecute(Boolean b){
            bidBut.setBackgroundColor(0xff669bff);
        }
    }

    private void resetClientView(String raw) throws JSONException {
        JSONArray array = new JSONArray(raw);
        clientList = "";
        for(int i = 0; i < array.length(); i++){
            JSONObject obj1 = (JSONObject) array.get(i);
            clientList += obj1.getString("client_name") + "\n";
        }
        clientView.setText(clientList);
    }

    private void resetBidList(String raw) throws JSONException {
        JSONArray array = new JSONArray(raw);
        dataToDisplay.clear();
        for(int i = 0; i < array.length(); i++){
            JSONObject obj = (JSONObject) array.get(i);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Utility.KEY_CURRENT_PRICE, "$" + obj.getString("price"));
            map.put(Utility.KEY_NAME, obj.getString("user_name"));
            map.put("time", obj.getString("time"));
            dataToDisplay.add(map);
        }
        Collections.reverse(dataToDisplay);
        adapter.updateView(dataToDisplay);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.livebid_bid_but:
                if(status == 2) {
                    if(Integer.valueOf(UserID) == seller_id){
                        toastLog("You cannot buy your own item!");
                        return;
                    }
                    openBidDialog();
                }else{
                    toastLog("You cannot bid at this moment!");
                }
                break;
            case R.id.livebid_image:
                openDialog();
                break;
            default:
                break;
        }
    }

    public void onDestroy(){
        if (wsC.isConnected()){
            wsC.disconnect();
        }
        needToastForLost = false;
        continueUpdate = false;
        super.onDestroy();
    }

    private void bid(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", "say");
            obj.put("to_client_id", "all");
            obj.put("to_client_name", null);
            obj.put("content", inputDialogPrice.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsC.sendTextMessage(obj.toString());
    }

    private boolean validateInput(){
        if(inputDialogPrice.getText().toString().equals("")){
            inputDialogError.setText("Please input your price!");
            return false;
        }
        float number = Float.valueOf(inputDialogPrice.getText().toString());
        if(number-current_price <= 9.99){
            inputDialogError.setText("Bid price should be at least $10 higher!\n"
                                    + "Current price is $" + current_price);
            return false;
        }
        return true;
    }

    private TextView inputDialogError;
    private EditText inputDialogPrice;
    private void openBidDialog(){
        View dialogView;
        LayoutInflater inflater = null;
        inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.live_bid_input, null);

        inputDialogError = (TextView) dialogView.findViewById(R.id.livebid_input_error);
        inputDialogPrice = (EditText) dialogView.findViewById(R.id.livebid_input_price);
        inputDialogPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        inputDialogPrice.setText(s);
                        inputDialogPrice.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    inputDialogPrice.setText(s);
                    inputDialogPrice.setSelection(2);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        inputDialogPrice.setText(s.subSequence(0, 1));
                        inputDialogPrice.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

        });

        TextView bid = (TextView) dialogView.findViewById(R.id.livebid_input_bid_but);
        bid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateInput()){
                    return;
                }
                bid();
                inputDialog.cancel();
            }
        });

        TextView cancel = (TextView) dialogView.findViewById(R.id.livebid_input_cancel_but);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDialog.cancel();
            }
        });

        inputDialog = new AlertDialog.Builder(this).create();
        inputDialog.show();
        inputDialog.getWindow().setContentView(dialogView);
        inputDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        inputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    private void openDialog(){
        View dialogView;
        LayoutInflater inflater = null;

        inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.dialog_view, null);
        ImageView dialogImage = (ImageView)dialogView.findViewById(R.id.dialog_image);
        TextView dialogDescriptionView = (TextView)dialogView.findViewById(R.id.dialog_description);
        TextView dialogSellerView = (TextView)dialogView.findViewById(R.id.dialog_seller_name);
        TextView dialogTitleView = (TextView)dialogView.findViewById(R.id.dialog_title);
        if(dialogTitle != null) {
            dialogTitleView.setText(dialogTitle);
        }
        if(imageBitmap != null){
            dialogImage.setImageBitmap(imageBitmap);
        }
        if(dialogDescription != null){
            dialogDescriptionView.setText(dialogDescription);
        }
        if(dialogSeller != null){
            dialogSellerView.setText(dialogSeller);
        }

        AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        dlg.getWindow().setContentView(dialogView);
    }

    private class AsyncDownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            try{
                URL url = new URL(image_name);
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
                imageBitmap = result;
                goodImage.setImageBitmap(result);
            }else{
                goodImage.setImageDrawable(getResources().getDrawable(R.drawable.hhh));
            }
        }
    }

}

