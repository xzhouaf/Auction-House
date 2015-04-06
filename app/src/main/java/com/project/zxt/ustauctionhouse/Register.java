package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Xutong on 2015/4/6.
 */
public class Register extends Activity implements View.OnClickListener {

    private final String TAG = "Register";
    private static Button registerBtn;
    private TextView userName, email, password;
    private EditText userNameReg, emailReg, passwordReg;
    TextView loginScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register);

        Log.i(TAG, "Activity: onCreate()");

        registerBtn = (Button) findViewById(R.id.btnRegister);
        registerBtn.setOnClickListener(this);

        userName = (TextView) findViewById(R.id.userName);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password);

        userNameReg = (EditText) findViewById(R.id.userNameReg);
        emailReg = (EditText) findViewById(R.id.emailReg);
        passwordReg = (EditText) findViewById(R.id.passwordReg);

        loginScreen = (TextView) findViewById(R.id.link_to_login);

        loginScreen.setOnClickListener(this);

    }

    public void onDestroy() {

        Log.i(TAG, "onDestroy()");


        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                //register
                break;
            case R.id.link_to_login:
                finish();
                break;
            default:
                break;


        }


    }
}
