/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.SearchItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.NewListView.RefreshListView;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


public class SearchResult extends Activity implements View.OnClickListener, Observer, RefreshListView.OnRefreshListener, RefreshListView.OnLoadListener  {

    private RefreshListView refreshLv;
    private LazyAdapter adapter;
    private GeneralSearch search;
    private ConditionCategoryLoader categoryLoader;
    private Context ctx;
    private Intent intent;
    private ArrayList<HashMap<String, String>> paramList;
    private LinearLayout priceAsc, priceDesc, timeAsc, timeDesc;
    private Spinner categorySel;
    private TextView searchBut;
    private EditText searchContainer;
    private String currentBut = "priceAsc", ApiKey, UserID;
    private static final int DARK_COLOR = 0xffe9e31d, BRIGHT_COLOR = 0xfffdff29;
    private String keyword, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_display_frame);
        intent = getIntent();

        priceAsc = (LinearLayout)findViewById(R.id.price_asc_touch);
        priceAsc.setOnClickListener(this);
        priceDesc = (LinearLayout)findViewById(R.id.price_desc_touch);
        priceDesc.setOnClickListener(this);
        timeAsc = (LinearLayout)findViewById(R.id.time_asc_touch);
        timeAsc.setOnClickListener(this);
        timeDesc = (LinearLayout)findViewById(R.id.time_desc_touch);
        timeDesc.setOnClickListener(this);

        categorySel = (Spinner) findViewById(R.id.search_category_again);
        searchBut = (TextView) findViewById(R.id.search_button_again);
        searchBut.setOnClickListener(this);
        searchContainer = (EditText) findViewById(R.id.search_again_container);

        keyword = intent.getStringExtra("keywords");
        category = intent.getStringExtra("category");
        UserID = intent.getStringExtra("user_ID");
        if(category.equals("All")) category = "";
        ApiKey = intent.getStringExtra("api");

        refreshLv=(RefreshListView)findViewById(R.id.search_result_frame_list);
        refreshLv.setOnRefreshListener(this);
        refreshLv.setOnLoadListener(this);

        isRefreshing = true;
        search = new GeneralSearch("0",category,keyword,"","","");
        search.addObserver(this);
        search.loadList();

        ctx = getApplicationContext();

        refreshLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position > paramList.size() || position <= 0) return;
                Intent intent = new Intent(ctx, ViewItem.class);
                intent.putExtra(Utility.KEY_IMAGE, paramList.get(position-1).get(Utility.KEY_IMAGE));
                intent.putExtra(Utility.KEY_ID,paramList.get(position-1).get(Utility.KEY_ID));
                intent.putExtra("user_ID", UserID);
                intent.putExtra("API_key", ApiKey);
                startActivity(intent);
            }
        });

        categoryLoader = new ConditionCategoryLoader(null, categorySel, this, false);
        categoryLoader.addObserver(this);
        categoryLoader.loadConditionCategory();
        searchContainer.setText(keyword);
    }


    @Override
    public void update(Observable observable, Object data) {
        if(observable == search) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            prepareDataForDisplay(true);
            adapter = new LazyAdapter(this, dataToDisplay);
            search.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            if (isRefreshing) {
                isRefreshing = false;
                refreshLv.refreshComplete();
                if(dataToDisplay.size() < 5){
                    refreshLv.loadComplete(true);
                }else{
                    refreshLv.loadComplete(false);
                }
            }
        }
        if(observable == categoryLoader){
            categorySel.setSelection(intent.getIntExtra("category_index", 0));
            categoryLoader.deleteObserver(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.search_button_again:
                keyword = searchContainer.getText().toString();
                category = categorySel.getSelectedItem().toString();
                isRefreshing = true;
                search = new GeneralSearch("0",category.equals("All")? null:category, keyword,"","","");
                search.addObserver(this);
                search.loadList();
                break;
            case R.id.price_asc_touch:
                currentBut = "priceAsc";
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","0","");
                onTabClick(priceAsc);
                break;
            case R.id.price_desc_touch:
                if(currentBut.equals("priceDesc")) return;
                currentBut = "priceDesc";
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","1","");
                onTabClick(priceDesc);
                break;
            case R.id.time_asc_touch:
                if(currentBut.equals("timeAsc")) return;
                currentBut = "timeAsc";
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","2","");
                onTabClick(timeAsc);
                break;
            case R.id.time_desc_touch:
                if(currentBut.equals("timeDesc")) return;
                currentBut = "timeDesc";
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","3","");
                onTabClick(timeDesc);
                break;
            default:
                break;
        }
    }

    private void onTabClick(LinearLayout l){
        isRefreshing = true;
        priceAsc.setBackgroundColor(DARK_COLOR);
        priceDesc.setBackgroundColor(DARK_COLOR);
        timeAsc.setBackgroundColor(DARK_COLOR);
        timeDesc.setBackgroundColor(DARK_COLOR);

        l.setBackgroundColor(BRIGHT_COLOR);

        search.addObserver(this);
        search.loadList();
    }

    private void initLoadData() {
        switch(currentBut){
            case "priceAsc":
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","0","");
                break;
            case "priceDesc":
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","1","");
                break;
            case "timeAsc":
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","2","");
                break;
            case "timeDesc":
                search = new GeneralSearch("0",category.equals("All")? null:category,keyword,"","3","");
                break;
            default:
                break;
        }
        search.addObserver(this);
        search.loadList();
    }

    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
    private int lastLoad = 0;
    private boolean isToEnd = false;

    private boolean prepareDataForDisplay(boolean isRefresh) {
        if (isRefresh) {
            dataToDisplay.clear();
            lastLoad = 0;
            isToEnd = false;
        }
        if(isToEnd) return false;
        for (int i = 0; i < 5; i++) {
            if (lastLoad + 1 > paramList.size()) {
                isToEnd = true;
                return false;
            }
            dataToDisplay.add(paramList.get(lastLoad));
            lastLoad++;
        }
        return true;
    }

    private void refreshLoadData() {
        initLoadData();
        refreshLv.loadComplete(false);
    }

    private void augmentLoadData() {
        prepareDataForDisplay(false);
        if (isToEnd) {
            refreshLv.loadComplete(true);
            return;
        }

        adapter.updateView(dataToDisplay);
        if(dataToDisplay.size() == paramList.size())
            refreshLv.loadComplete(true);
        else refreshLv.loadComplete(false);
    }

    boolean isRefreshing = false;

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                isRefreshing = true;
                refreshLoadData();

            }
        }, 1000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                augmentLoadData();
            }
        }, 500);
    }
}

