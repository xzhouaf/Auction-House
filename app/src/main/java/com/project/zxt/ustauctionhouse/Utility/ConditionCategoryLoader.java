/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */

package com.project.zxt.ustauctionhouse.Utility;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;


public class ConditionCategoryLoader extends Observable {

    private Spinner condition = null;
    private Spinner category = null;
    private Activity parent;
    private boolean isPost;

    public ConditionCategoryLoader(Spinner con, Spinner cat, Activity pa, boolean isPostItem){
        condition = con;
        category = cat;
        parent = pa;
        isPost = isPostItem;
    }

    public void loadConditionCategory(){
        new AsyncGetConditionCategory().execute();
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
                Toast.makeText(parent, "Cannot connect to server now. Make sure you connect to " +
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

            if(!obj.isNull("condition") && !(condition == null)){
                JSONArray conditionArray = obj.getJSONArray("condition");
                String conditionNewArray[] = new String[conditionArray.length()];
                for(int i = 0; i < conditionArray.length(); i++){
                    conditionNewArray[i] = (String)((JSONObject)conditionArray.get(i)).get("condition_name");
                    //Log.i("Important: ", conditionNewArray[i]);
                }
                ArrayAdapter adapter=new ArrayAdapter(parent.getApplicationContext(),R.layout.login_list_item,conditionNewArray);
                condition.setAdapter(adapter);
            }

            if(!obj.isNull("category") && !(category == null)){
                JSONArray categoryArray = obj.getJSONArray("category");
                String categoryNewArray[];
                if(isPost) {
                    categoryNewArray = new String[categoryArray.length()];
                    for (int i = 0; i < categoryArray.length(); i++) {
                        categoryNewArray[i] = (String) ((JSONObject) categoryArray.get(i)).get("category_name");
                    }
                }else{
                    categoryNewArray = new String[categoryArray.length()+1];
                    categoryNewArray[0] = "All";
                    for (int i = 0; i < categoryArray.length(); i++) {
                        categoryNewArray[i + 1] = (String) ((JSONObject) categoryArray.get(i)).get("category_name");
                    }
                }
                ArrayAdapter adapter=new ArrayAdapter(parent.getApplicationContext(), R.layout.login_list_item,categoryNewArray);
                category.setAdapter(adapter);
            }
            setChanged();
            notifyObservers(null);
        }
    }

}
