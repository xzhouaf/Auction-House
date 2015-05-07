package com.project.zxt.ustauctionhouse.LiveAuction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.LoginRelated.LogIn;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by paul on 15年5月3日.
 *
 */
public class TCP{

    private Handler hd ;
    private BufferedReader buf ;
    static Socket sock ;
    private Bundle bun;
    private boolean continueUpdate = true;

    public TCP(Handler hd) {
        this.hd = hd;
        new AsyncSend().execute();
    }

    public void destroy() throws IOException {
        continueUpdate = false;
        sock.close();
    }

    private class AsyncSend extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {


            JSONObject obj = null;
            String host = "";
            int port = 0;
            String url1 = Utility.serverUrl + "/getHostPort";
            HttpGet httpGet = new HttpGet(url1);
            HttpClient httpClient = new DefaultHttpClient();
            try{
                HttpResponse response = httpClient.execute(httpGet);
                obj = Utility.response2obj(response);
                host = obj.getString("host");
                port = obj.getInt("port");
            }catch (Exception e){
                e.printStackTrace();
            }


            sock = new Socket();
            bun = new Bundle();

            InetSocketAddress isa =
                    new InetSocketAddress(host, port);
            try {
                sock.connect(isa, 1000);//如果1000ms没有连接到就会失败
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            new Thread(new messtake()).start();

        }
    }

    private class messtake implements Runnable{

        @Override
        public void run() {
            String msg ="";
            try {
                while(continueUpdate) {
                    buf = new BufferedReader(
                            new InputStreamReader(sock.getInputStream()));
                    while ((msg = buf.readLine()) != null) {
                        if (msg.trim().equals("")) {//这里会接收到一个空白字符串，还不知道为什么 ！
                            continue;
                        }
                        //先把字符串中原本的/n替换掉，客户端接收再替换过来
                        //一下是android部分通知ui更新

                        Log.i("Debugging::: ", msg);

                        bun.putString("contentMess", msg);
                        bun.putInt("type", 1);
                        Message message = new Message();
                        message.setData(bun);
                        hd.sendMessage(message);
                        Thread.sleep(20);
                    }
                    Thread.sleep(20);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //向服务器发送消息
    public void send(String content)
    {
        try {
            OutputStream ou = sock.getOutputStream();
            //ou.write(content.getBytes().length);
            ou.write(content.getBytes());
            ou.flush();

        } catch (IOException e) {
            bun.putString("contentMess", "发送失败:" + content);
            bun.putInt("type", 0);
            Message message = new Message();
            message.setData(bun);
            hd.sendMessage(message);
            e.printStackTrace();
        }
    }
}