/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.LoginRelated;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.project.zxt.ustauctionhouse.R;
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

import java.util.ArrayList;
import java.util.List;


public class Register extends Activity implements View.OnClickListener {

    private final String TAG = "Register";
    //private static Button registerBtn;
    //private TextView userName, email, password;
    private EditText userNameReg, emailReg, passwordReg;
    private TextView loginScreen;
    private Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register);

        Log.i(TAG, "Activity: onCreate()");

        Button registerBtn = (Button) findViewById(R.id.btnRegister);
        registerBtn.setOnClickListener(this);

        /*userName = (TextView) findViewById(R.id.userName);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password);*/

        userNameReg = (EditText) findViewById(R.id.userNameReg);
        emailReg = (EditText) findViewById(R.id.emailReg);
        passwordReg = (EditText) findViewById(R.id.passwordReg);

        loginScreen = (TextView) findViewById(R.id.link_to_login);
        loginScreen.setOnClickListener(this);

        ctx = getApplicationContext();
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
                if(!Utility.isOnline(ctx)){
                    Toast.makeText(ctx,"Please check your network connection!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(validateInput()) {
                    new AsyncRegister().execute();
                }
                break;
            case R.id.link_to_login:
                Intent intent = new Intent(ctx, LogIn.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            default:
                break;
        }
    }

    private boolean validateInput(){
        final String user = userNameReg.getText().toString();
        if (!Utility.isValidUserName(user)) {
            userNameReg.setError("Invalid UserName");
            return false;
        }

        final String email_ = emailReg.getText().toString();
        if (!Utility.isValidEmail(email_)) {
            emailReg.setError("Invalid Email");
            return false;
        }

        final String pass = passwordReg.getText().toString();
        if (!Utility.isValidPassword(pass)) {
            passwordReg.setError("Invalid Password");
            return false;
        }
        return true;
    }

    private class AsyncRegister extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("name", userNameReg.getText().toString());
            NameValuePair pair2 = new BasicNameValuePair("email", emailReg.getText().toString());
            NameValuePair pair3 = new BasicNameValuePair("password", passwordReg.getText().toString());
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/register");
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
                    emailReg.setText("");
                    passwordReg.setText("");
                }else{
                    Intent intent = new Intent(ctx, LogIn.class);
                    intent.putExtra("emailForCompleteLogin", emailReg.getText().toString());
                    intent.putExtra("passForCompleteLogin", passwordReg.getText().toString());
                    startActivity(intent);
                    //overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                    finish();
                    //setResult(RESULT_OK, intent);
                    //overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private long mExitTime;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "Double click to exit UST Auction", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
