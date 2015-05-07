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

import com.project.zxt.ustauctionhouse.R;

import java.io.IOException;

/**
 * Created by paul on 15年5月4日.
 *
 */
public class Live extends Activity implements View.OnClickListener {

    private final String TAG = "Live";
    private Context ctx;
    private Intent intent;
    private TextView returnText;
    private EditText sendText;
    private Button sendBut;
    private TCP communication;
    private Handler myHandler;
    private String messageToUpdate;

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            returnText.setText(messageToUpdate);
            myHandler.postDelayed(update, 10);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.live);
        ctx = getApplicationContext();
        intent = getIntent();

        returnText = (TextView) findViewById(R.id.returnMessage);
        sendText = (EditText) findViewById(R.id.sendText);
        sendBut = (Button) findViewById(R.id.sendTCP);
        sendBut.setOnClickListener(this);
        messageToUpdate = "Initial Text";

        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.getData().getInt("type")){
                    case 0:
                        messageToUpdate = messageToUpdate + "\n" + "Self: " + msg.getData().getString("contentMess");
                        break;
                    case 1:
                        messageToUpdate = messageToUpdate + "\n" + msg.getData().getString("contentMess");
                        break;
                    default:
                        break;
                }

            }
        };
        myHandler.post(update);
        communication = new TCP(myHandler);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendTCP:
                communication.send(sendText.getText().toString());
                break;
            default:
                break;
        }
    }

    public void onDestroy(){
        try {
            communication.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
