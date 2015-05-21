/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.LoginRelated;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.project.zxt.ustauctionhouse.bottomMenu.BottomMenuHome;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.GCM.RegisterApp;
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

public class LogIn extends Activity implements View.OnClickListener {

    private static final String TAG = "LogIn";
    //private TextView userName, password;
    private EditText  passwordInput;
    private TextView registerScreen;
    private Context ctx;
    AutoCompleteTextView userNameInput;
    private InputMethodManager imm;
    private Intent intent;

    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Log.i(TAG, "Activity: onCreate()");

        Utility.initializeDirectory();
        Button loginBtn = (Button) findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(this);
        imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        passwordInput = (EditText) findViewById(R.id.passwordInput);
        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login();
                    handled = true;
                }
                return handled;
            }
        });

        registerScreen = (TextView) findViewById(R.id.link_to_register);
        registerScreen.setOnClickListener(this);

        ctx = this.getApplicationContext();
        String[] countries = getSavedLoginEmail();
        userNameInput = (AutoCompleteTextView)findViewById(R.id.userNameInput);//找到相应的控件
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.login_list_item, countries);//配置Adaptor
        userNameInput.setAdapter(adapter);

        intent = getIntent();
        if(intent.getStringExtra("emailForCompleteLogin") != null){
            userNameInput.setText(intent.getStringExtra("emailForCompleteLogin"));
            passwordInput.setText(intent.getStringExtra("passForCompleteLogin"));
            intent.putExtra("emailForCompleteLogin", "");
            intent.putExtra("passForCompleteLogin", "");
        }

        checkLoggedIn();

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
                login();
                break;
            case R.id.link_to_register:
                Intent i = new Intent(ctx, Register.class);
                startActivity(i);
                //overridePendingTransition(R.anim.out_alpha, R.anim.enter_alpha);
                finish();
                break;
            default:
                break;
        }
    }

    private void login(){
        //login
        if(!Utility.isOnline(ctx)){
            imm.hideSoftInputFromWindow(passwordInput.getWindowToken(), 0);
            Toast.makeText(ctx,"Please check your network connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(validateInput()) {
            imm.hideSoftInputFromWindow(passwordInput.getWindowToken(), 0);
            new AsyncLogin().execute();
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

    private void saveLoginEmail(String s){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor prefed = prefs.edit();
        prefed.putString("loginHistoryEmail", s);
        prefed.apply();
    }

    private String[] getSavedLoginEmail(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String a = prefs.getString("loginHistoryEmail", null);
        if(a != null){
            String result[] = new String[1];
            result[0] = a;
            return result;
        }
        String result[] = new String[1];
        result[0] = " ";
        return result;
    }

    private void saveLoggedInUser(String a, String b, String c, String d, String e){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor prefed = prefs.edit();
        prefed.putString("loggedInA", a);
        prefed.putString("loggedInB", b);
        prefed.putString("loggedInC", c);
        prefed.putString("loggedInD", d);
        prefed.putString("loggedInE", e);
        prefed.apply();
    }

    private void checkLoggedIn(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String a = prefs.getString("loggedInA", null);
        String b = prefs.getString("loggedInB", null);
        String c = prefs.getString("loggedInC", null);
        String d = prefs.getString("loggedInD", null);
        String e = prefs.getString("loggedInE", null);
        if(a == null){
            return;
        }else{
            jumpToMainActivity(a,b,c,d,e);
        }
    }

    private class AsyncLogin extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            NameValuePair pair1 = new BasicNameValuePair("email", userNameInput.getText().toString());
            NameValuePair pair2 = new BasicNameValuePair("password", passwordInput.getText().toString());
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,"UTF-8");
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(Utility.serverUrl + "/login");
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
                Toast.makeText(ctx, "Cannot connect to server now. " +
                        "Make sure the server is turned on!", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                switch(result.getString("error")){
                    case "true":
                        Toast.makeText(ctx, result.getString("message"), Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                        break;
                    case "false":
                        String fetchedUserName = result.getString("name");
                        String fetchedEmail = result.getString("email");
                        String fetchedApiKey = result.getString("apiKey");
                        String fetchedCreatedAt = result.getString("createdAt");
                        String fetchedUserID = result.getString("user_id");
                        new RegisterApp(fetchedApiKey ,ctx, GoogleCloudMessaging.getInstance(ctx), Utility.getAppVersion(ctx)).execute();

                        saveLoginEmail(userNameInput.getText().toString());
                        saveLoggedInUser(fetchedUserName, fetchedEmail, fetchedApiKey, fetchedCreatedAt, fetchedUserID);
                        jumpToMainActivity(fetchedUserName, fetchedEmail, fetchedApiKey, fetchedCreatedAt, fetchedUserID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void jumpToMainActivity(final String a, String b, String c, String d, String e){
        final Intent i = new Intent(ctx, BottomMenuHome.class);
        i.putExtra("user_name", a);
        i.putExtra("user_email", b);
        i.putExtra("user_apiKey", c);
        i.putExtra("user_createdAt", d);
        i.putExtra("user_ID", e);
        i.putExtra("current_tab", "New Item");
        setContentView(R.layout.welcome);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startActivity(i);
                Toast.makeText(ctx, "Welcome, " + a, Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 1000);

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
