package com.project.zxt.ustauctionhouse.SearchItem;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.bottomMenuActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.Condition;

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
    private EditText searchKeyword;

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

        searchKeyword = (EditText) findViewById(R.id.searchInput);

        new ConditionCategoryLoader(null, searchCategory, this).loadConditionCategory();

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
                intent.putExtra("keywords", searchKeyword.getText().toString());
                intent.putExtra("category", searchCategory.getSelectedItem().toString());
                intent.putExtra("category_index", searchCategory.getSelectedItemPosition());
                intent.putExtra("api", ApiKey);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}