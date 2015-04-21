package com.project.zxt.ustauctionhouse.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.SearchItem.SearchResult;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

/**
 * Created by Paul on 2015/4/14.
 *
 */
public class GeneralSearch extends Observable {
    private static final String TAG = "General Search";
    private String status, category, keywords, user_api, orderBy, identity;

    public GeneralSearch(String s, String c, String k, String u, String o, String i){

        status = s;
        category = c;
        keywords = k;
        user_api = u;
        orderBy = o;
        identity = i;

    }

    public void loadList(){
        new AsyncSearch().execute();
    }

    private class AsyncSearch extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            NameValuePair pair1 = new BasicNameValuePair("status", status);
            NameValuePair pair2 = new BasicNameValuePair("category", category);
            NameValuePair pair3 = new BasicNameValuePair("keywords", keywords);
            NameValuePair pair4 = new BasicNameValuePair("order", orderBy);
            NameValuePair pair5 = new BasicNameValuePair("identity", identity);
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            pairList.add(pair2);
            pairList.add(pair3);
            pairList.add(pair4);
            pairList.add(pair5);
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = null;
                if(!user_api.equals("")) {
                    httpPost = new HttpPost(Utility.serverUrl + "/itemsByUser");
                    httpPost.addHeader("Authorization", user_api);
                }else{
                    httpPost = new HttpPost(Utility.serverUrl + "/searchItems");
                }
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);

                String line = "";
                HttpEntity receivedEntity = response.getEntity();
                InputStream receivedStream = receivedEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        receivedStream));
                while (null != (line = reader.readLine())) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result){
            List<Unit> unitList = null;
            try {
                unitList = Utility.string2unit(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<HashMap<String, String>> goodList = new ArrayList<HashMap<String, String>>();
            if(unitList != null) {
                for (int i = 0; i < unitList.size(); i++) {
                    // 新建一个 HashMap
                    HashMap<String, String> map1 = new HashMap<String, String>();
                    //每个子节点添加到HashMap 密匙 => 值
                    map1.put(Utility.KEY_ID, unitList.get(i).id + "");
                    map1.put(Utility.KEY_NAME, unitList.get(i).name);
                    map1.put(Utility.KEY_SELLER, unitList.get(i).userName);
                    map1.put(Utility.KEY_TIME_LEFT, (unitList.get(i).timeLeft) / 3600 + " hr " + ((unitList.get(i).timeLeft) % 3600) / 60 + " min");
                    map1.put(Utility.KEY_CURRENT_PRICE, "$" + unitList.get(i).currentPrice);
                    map1.put(Utility.KEY_IMAGE, Utility.serverUrl + "/uploads/" + unitList.get(i).imageFileName);
                    map1.put(Utility.KEY_HIGHEST_BIDDER, (unitList.get(i).buyerName == null)? "No bidder yet" : unitList.get(i).buyerName);
                    map1.put(Utility.KEY_HIGHEST_BIDDER_ID, unitList.get(i).buyerID+"");

                    // HashList添加到数组列表
                    goodList.add(map1);
                }
            }

            setChanged();
            notifyObservers(goodList);
        }
    }
}
