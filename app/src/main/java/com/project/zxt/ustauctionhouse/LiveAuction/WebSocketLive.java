package com.project.zxt.ustauctionhouse.LiveAuction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnection;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnectionHandler;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketException;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Button sendBut;
    private String receivedMessage = "";
    private String ApiKey, UserID;

    public static String wsUrl = "ws://gaozihou.no-ip.org:3636";
    public WebSocketConnection wsC = new WebSocketConnection();

    /* Seems to be useless */
    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            super.handleMessage(msg);
            if ( msg.what == 0 ){

            }
        }
    };

    public void toastLog( String s )
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.live);
        ctx = getApplicationContext();
        intent = getIntent();

        ApiKey = intent.getStringExtra("user_apiKey");
        UserID = intent.getStringExtra("user_ID");

        returnText = (TextView) findViewById(R.id.returnMessage);
        sendText = (EditText) findViewById(R.id.sendText);
        timeText = (TextView) findViewById(R.id.timeText);
        sendBut = (Button) findViewById(R.id.sendTCP);
        sendBut.setOnClickListener(this);

        wsStart();

    }

    private void wsStart()
    {
        try {
            wsC.connect( wsUrl, new WebSocketConnectionHandler()
            {
                @Override
                public void onOpen()
                {
                    toastLog( "Status: Connected to " + wsUrl );

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", "authenticate");
                        obj.put("api_key", ApiKey);
                        obj.put("item_id", 1);
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
                            case "login":
                                receivedMessage += "User "+obj.getInt("from_id")+" login.\n";
                                returnText.setText(receivedMessage);
                                timeText.setText(obj.getString("time"));
                                break;
                            case "logout":
                                receivedMessage += "User "+obj.getInt("from_id")+" logout.\n";
                                returnText.setText(receivedMessage);
                                break;
                            case "say":
                                receivedMessage += "User "+obj.getInt("from_id")+" say: "
                                        + obj.getString("content") + " " + obj.getInt("price") + "\n";
                                returnText.setText(receivedMessage);
                                break;
                            case "authenticate":
                                receivedMessage += obj.getString("content") + "\n";
                                returnText.setText(receivedMessage);
                                break;
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
                    toastLog( "Connection lost." );
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
                    obj.put("type", "bid");
                    obj.put("message", sendText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                wsC.sendTextMessage(obj.toString());
                sendText.setText("");
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

