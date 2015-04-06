package com.project.zxt.ustauctionhouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.project.zxt.ustauctionhouse.Utility;

/**
 * Created by Xutong on 2015/4/6.
 */
public class Register extends Activity implements View.OnClickListener {

    private final String TAG = "Register";
    private static Button registerBtn;
    private TextView userName, email, password;
    private EditText userNameReg, emailReg, passwordReg;
    TextView loginScreen;
    private Context ctx;

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
                validateInput();
                new AsyncRegister().execute();
                break;
            case R.id.link_to_login:
                finish();
                break;
            default:
                break;


        }
    }

    private boolean validateInput(){
        //TODO Check the correctness of input values
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
            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("name", userNameReg.getText().toString());
            NameValuePair pair2 = new BasicNameValuePair("email", emailReg.getText().toString());
            NameValuePair pair3 = new BasicNameValuePair("password", passwordReg.getText().toString());
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverurl + "/register");
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
            try {
                Toast.makeText(ctx, result.getString("message"), Toast.LENGTH_SHORT).show();
                if(result.getString("error").equals("true")){
                    emailReg.setText("");
                    passwordReg.setText("");
                }else{
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
