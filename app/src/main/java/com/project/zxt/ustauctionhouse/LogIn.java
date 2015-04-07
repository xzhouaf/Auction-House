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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

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

/**
 * Created by Xutong on 2015/4/6.
 */
public class LogIn extends Activity implements View.OnClickListener {

    private static final String TAG = "LogIn";
    private static Button loginBtn;
    private TextView userName, password;
    private EditText userNameInput, passwordInput;
    TextView registerScreen;
    private Context ctx;

    private String fetchedUserName, fetchedEmail, fetchedApiKey, fetchedCreatedAt;

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

        ctx = getApplicationContext();

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
                if(!Utility.isOnline(ctx)){
                    Toast.makeText(ctx,"Please check your network connection!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(validateInput()) {
                    new AsyncLogin().execute();
                }
                break;
            case R.id.link_to_register:
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    private boolean validateInput(){

        final String email_ = userNameInput.getText().toString();
        if (!Utility.isValidEmail(email_)) {
            userNameInput.setError("Invalid Email");
            return false;
        }

        final String pass = passwordInput.getText().toString();
        if (!Utility.isValidPassword(pass)) {
            passwordInput.setError("Invalid Password");
            return false;
        }
        return true;
    }

    private class AsyncLogin extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("email", userNameInput.getText().toString());
            NameValuePair pair2 = new BasicNameValuePair("password", passwordInput.getText().toString());
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair1);
            pairList.add(pair2);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverurl + "/login");
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
                switch(result.getString("error")){
                    case "true":
                        Toast.makeText(ctx, result.getString("message"), Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                        break;
                    case "false":
                        fetchedUserName = result.getString("name");
                        fetchedEmail = result.getString("email");
                        fetchedApiKey = result.getString("apiKey");
                        fetchedCreatedAt = result.getString("createdAt");
                        new RegisterApp(fetchedApiKey ,ctx, GoogleCloudMessaging.getInstance(ctx), Utility.getAppVersion(ctx)).execute();

                        Toast.makeText(ctx, "Welcome, " + fetchedUserName, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(ctx, PersonalInformation.class);
                        i.putExtra("user_name", fetchedUserName);
                        i.putExtra("user_email", fetchedEmail);
                        i.putExtra("user_apiKey", fetchedApiKey);
                        i.putExtra("user_createdAt", fetchedCreatedAt);
                        startActivity(i);
                        finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
