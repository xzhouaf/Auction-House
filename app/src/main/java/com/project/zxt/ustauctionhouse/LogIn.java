package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Xutong on 2015/4/6.
 */
public class LogIn extends Activity implements View.OnClickListener {

    private static final String TAG = "LogIn";
    private static Button loginBtn;
    private TextView userName, password;
    private EditText userNameInput, passwordInput;
    TextView registerScreen;


    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        Log.i(TAG, "Activity: onCreate()");

        loginBtn = (Button) findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(this);

        userName = (TextView) findViewById(R.id.userName);
        password = (TextView) findViewById(R.id.password);

        userNameInput = (EditText) findViewById(R.id.userNameInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);

        registerScreen = (TextView) findViewById(R.id.link_to_register);

        registerScreen.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy()");


        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                //login
                break;
            case R.id.link_to_register:
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
                break;
            default:
                break;



        }



    }
}
