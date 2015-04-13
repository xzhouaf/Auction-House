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
import android.widget.ListView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.R;
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


public class SearchResult extends Activity implements Observer {

    private ListView list;
    private LazyAdapter adapter;
    private GeneralSearch search;
    private Context ctx;
    private Intent intent;
    private ArrayList<HashMap<String, String>> paramList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_display_frame);
        intent = getIntent();

        String keyword = intent.getStringExtra("keywords");
        String category = intent.getStringExtra("category");
        if(category.equals("All")) category = "";

        list=(ListView)findViewById(R.id.frame_list);
        search = new GeneralSearch("",category,keyword,"","","");
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
                startActivity(intent);
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        if(observable == search) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            adapter = new LazyAdapter(this, paramList);
            search.deleteObserver(this);
            list.setAdapter(adapter);
        }
    }
}


