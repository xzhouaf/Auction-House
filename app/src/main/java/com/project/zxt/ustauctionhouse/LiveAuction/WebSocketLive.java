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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 15年5月7日.
 *
 */
public class WebSocketLive extends Activity implements View.OnClickListener {

    private final String TAG = "WebSocketLive";
    private Context ctx;
    private Intent intent;
    private TextView timeText;
    private TextView bidBut;
    private String ApiKey, UserID, UserName;
    private boolean needReconnect = true;
    private String roomID, image_name;
    private ListView bidList;
    private LiveBidAdapter adapter;
    private ArrayList<HashMap<String, String>> goodList;
    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
    private ImageLoader imageLoader;
    private ImageView goodImage;
    private Bitmap imageBitmap;
    public WebSocketConnection wsC = new WebSocketConnection();

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



        bidList = (ListView) findViewById(R.id.livebid_listview);
        //for debug
        dataToDisplay.add(new HashMap<String, String>());
        dataToDisplay.add(new HashMap<String, String>());
        dataToDisplay.add(new HashMap<String, String>());
        //debug end
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
                    toastLog( "Status: Connected to " + Utility.wsUrl );
                    needReconnect = false;

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
                            case "time":
                                timeText.setText(obj.getString("time"));
                                break;
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
                                /*
                                receivedMessage += "User "+obj.getInt("client_id")+" login.\n";
                                returnText.setText(receivedMessage);
                                timeText.setText(obj.getString("time"));
                                break;
                                */
                            case "logout":
                                /*
                                receivedMessage += "User "+obj.getInt("from_client_id")+" logout.\n";
                                returnText.setText(receivedMessage);
                                break;
                                */
                            case "say":/*
                                receivedMessage += "User "+obj.getInt("from_client_id")+" say: "
                                        + obj.getString("content") + " and current price is " +
                                        obj.getInt("price") + "\n";
                                returnText.setText(receivedMessage);
                                break;
                                */
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose( int code, String reason )
                {
                    toastLog( "Connection lost" );
                    needReconnect = true;
                }
            } );
        } catch ( WebSocketException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendTCP:
                JSONObject obj = new JSONObject();
                try {
                    obj.put("type", "say");
                    obj.put("to_client_id", "all");
                    obj.put("to_client_name", null);
                    //obj.put("content", sendText.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                wsC.sendTextMessage(obj.toString());
                //sendText.setText("");
                break;
            case R.id.reconBut:
                if(needReconnect){
                    wsStart();
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
        super.onDestroy();
    }


    private void openDialog(){
        View dialogView;
        LayoutInflater inflater = null;

        inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.dialog_view, null);
        ImageView dialogImage = (ImageView)dialogView.findViewById(R.id.dialog_image);
        TextView dialogDescription = (TextView)dialogView.findViewById(R.id.dialog_description);
        TextView dialogSeller = (TextView)dialogView.findViewById(R.id.dialog_seller_name);
        TextView dialogTitle = (TextView)dialogView.findViewById(R.id.dialog_title);
        dialogTitle.setText("Hihi");
        if(imageBitmap != null){
            dialogImage.setImageBitmap(imageBitmap);
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

