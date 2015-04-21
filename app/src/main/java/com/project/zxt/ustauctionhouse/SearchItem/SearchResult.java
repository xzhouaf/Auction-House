package com.project.zxt.ustauctionhouse.SearchItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Unit;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Condition;


public class SearchResult extends Activity implements View.OnClickListener, Observer {

    private ListView list;
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

        list=(ListView)findViewById(R.id.search_result_frame_list);
        search = new GeneralSearch("0",category,keyword,"","","");
        search.addObserver(this);
        search.loadList();

        ctx = getApplicationContext();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //这里可以自由发挥，比如播放一首歌曲等等
                Log.i("onClickEntry: ", "Position is " + position);
                Log.i("onClickEntry: ", "task_ID is " + paramList.get(position).get("id"));
                Intent intent = new Intent(ctx, ViewItem.class);
                intent.putExtra(Utility.KEY_IMAGE, paramList.get(position).get(Utility.KEY_IMAGE));
                intent.putExtra(Utility.KEY_ID,paramList.get(position).get(Utility.KEY_ID));
                intent.putExtra("user_ID", UserID);
                startActivity(intent);
            }
        });

        categoryLoader = new ConditionCategoryLoader(null, categorySel, this);
        categoryLoader.addObserver(this);
        categoryLoader.loadConditionCategory();
        searchContainer.setText(keyword);
    }

    @Override
    public void update(Observable observable, Object data) {
        if(observable == search) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            adapter = new LazyAdapter(this, paramList);
            search.deleteObserver(this);
            list.setAdapter(adapter);
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
                search = new GeneralSearch("0",category.equals("All")? null:category, keyword,"","","");
                search.addObserver(this);
                search.loadList();
                break;
            case R.id.price_asc_touch:
                if(currentBut.equals("priceAsc")) return;
                currentBut = "priceAsc";
                search = new GeneralSearch("0",category,keyword,"","0","");
                onTabClick(priceAsc);
                break;
            case R.id.price_desc_touch:
                if(currentBut.equals("priceDesc")) return;
                currentBut = "priceDesc";
                search = new GeneralSearch("0",category,keyword,"","1","");
                onTabClick(priceDesc);
                break;
            case R.id.time_asc_touch:
                if(currentBut.equals("timeAsc")) return;
                currentBut = "timeAsc";
                search = new GeneralSearch("0",category,keyword,"","2","");
                onTabClick(timeAsc);
                break;
            case R.id.time_desc_touch:
                if(currentBut.equals("timeDesc")) return;
                currentBut = "timeDesc";
                search = new GeneralSearch("0",category,keyword,"","3","");
                onTabClick(timeDesc);
                break;
            default:
                break;
        }
    }

    private void onTabClick(LinearLayout l){
        priceAsc.setBackgroundColor(DARK_COLOR);
        priceDesc.setBackgroundColor(DARK_COLOR);
        timeAsc.setBackgroundColor(DARK_COLOR);
        timeDesc.setBackgroundColor(DARK_COLOR);

        l.setBackgroundColor(BRIGHT_COLOR);

        search.addObserver(this);
        search.loadList();
    }
}

