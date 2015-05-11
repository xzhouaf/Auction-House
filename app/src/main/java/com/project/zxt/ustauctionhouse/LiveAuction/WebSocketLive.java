package com.project.zxt.ustauctionhouse.LiveAuction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.LiveUnit;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnection;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnectionHandler;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

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
    private TextView returnText, timeText;
    private EditText sendText;
    private Button sendBut, reconBut, reset_list_but;
    private String receivedMessage = "";
    private String ApiKey, UserID, UserName;
    private boolean needReconnect = true;
    private String roomID;
    private ListView bidList;
    private LiveBidAdapter adapter;
    private ArrayList<HashMap<String, String>> goodList;
    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
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

        timeText = (TextView) findViewById(R.id.livebid_curr_time);

        bidList = (ListView) findViewById(R.id.livebid_listview);
        //for debug
        dataToDisplay.add(new HashMap<String, String>());
        dataToDisplay.add(new HashMap<String, String>());
        dataToDisplay.add(new HashMap<String, String>());
        //debug end
        adapter = new LiveBidAdapter(this, dataToDisplay);
        bidList.setAdapter(adapter);

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
                    obj.put("content", sendText.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                wsC.sendTextMessage(obj.toString());
                sendText.setText("");
                break;
            case R.id.reconBut:
                if(needReconnect){
                    wsStart();
                }
                break;
            case R.id.get_list_but:

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

}

