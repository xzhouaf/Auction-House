package com.project.zxt.ustauctionhouse.SearchItem;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Paul on 2015/4/12.
 *
 */

public class Search extends bottomMenuActivity implements View.OnClickListener{
    private static final String TAG = "Search";
    private String UserName, Email, ApiKey, CreatedAt;
    private Intent intent;
    private Context ctx;
    private Spinner searchCategory;

    public int getContentViewLayoutResId() { return R.layout.search; }

    final protected void onCreatOverride(Bundle savedInstanceState) {

        Log.i(TAG, "Subclass Activity: onCreate()");

        intent = this.getIntent();
        ctx = getApplicationContext();
        UserName = intent.getStringExtra("user_name");
        Email = intent.getStringExtra("user_email");
        ApiKey = intent.getStringExtra("user_apiKey");
        CreatedAt = intent.getStringExtra("user_createdAt");

        Log.i(TAG, UserName + ", " + Email + ", " + ApiKey + ", " + CreatedAt);

        searchCategory = (Spinner) findViewById(R.id.searchCategorySel);

        Button testToResult = (Button)findViewById(R.id.searchButton);
        testToResult.setOnClickListener(this);

        new AsyncGetConditionCategory().execute();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                Intent intent = new Intent(ctx, SearchResult.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class AsyncGetConditionCategory extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            if(!Utility.serverTest()){
                return null;
            }
            JSONObject obj = null;
            // 使用GET方法发送请求,需要把参数加在URL后面，用？连接，参数之间用&分隔
            String url1 = Utility.serverUrl + "/conditionAndCategory";
            // 生成请求对象
            HttpGet httpGet = new HttpGet(url1);
            HttpClient httpClient = new DefaultHttpClient();
            // 发送请求
            try{
                HttpResponse response = httpClient.execute(httpGet);
                obj = Utility.response2obj(response);
            }catch (Exception e){
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
                if(result.getString("error").equals("true")){
                    Log.i("Update Failed!!!", "cannot update condition category");
                }else{
                    Log.i("Update Result:", result.toString());
                    jsonAnalysis(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void jsonAnalysis(JSONObject obj) throws JSONException {
            /*
            if(!obj.isNull("condition")){
                JSONArray conditionArray = obj.getJSONArray("condition");
                String conditionNewArray[] = new String[conditionArray.length()];
                for(int i = 0; i < conditionArray.length(); i++){
                    conditionNewArray[i] = (String)((JSONObject)conditionArray.get(i)).get("condition_name");
                    //Log.i("Important: ", conditionNewArray[i]);
                }
                ArrayAdapter adapter=new ArrayAdapter(getApplicationContext(),R.layout.login_list_item,conditionNewArray);
                condition_name.setAdapter(adapter);
            }
            */
            if(!obj.isNull("category")){
                JSONArray categoryArray = obj.getJSONArray("category");
                String categoryNewArray[] = new String[categoryArray.length()+1];
                categoryNewArray[0] = "";
                for(int i = 0; i < categoryArray.length(); i++){
                    categoryNewArray[i+1] = (String)((JSONObject)categoryArray.get(i)).get("category_name");
                    //Log.i("Important: ", categoryNewArray[i]);
                }
                ArrayAdapter adapter=new ArrayAdapter(getApplicationContext(),R.layout.login_list_item,categoryNewArray);
                searchCategory.setAdapter(adapter);
            }
        }
    }

}