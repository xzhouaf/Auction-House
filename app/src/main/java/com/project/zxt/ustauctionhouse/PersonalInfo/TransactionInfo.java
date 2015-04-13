package com.project.zxt.ustauctionhouse.PersonalInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyAdapter;
import com.project.zxt.ustauctionhouse.ItemListView.LazyMyBidAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.GeneralSearch;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.ViewItem.ViewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Paul on 2015/4/13.
 *
 */
public class TransactionInfo extends Activity implements View.OnClickListener, Observer {

    private ListView list;
    private LinearLayout sellingBut, biddingBut, sellHisBut, bidHisBut;
    private String currentBut, ApiKey;
    private Context ctx;
    private Intent intent;
    private static final int DARK_COLOR = 0xffe9e31d, BRIGHT_COLOR = 0xfffdff29;
    private TextView prev, next, blank;
    private GeneralSearch search;
    private ArrayList<HashMap<String, String>> paramList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_info_frame);
        list=(ListView)findViewById(R.id.transaction_info_frame_list);



        sellingBut = (LinearLayout)findViewById(R.id.selling_touch);
        sellingBut.setOnClickListener(this);
        biddingBut = (LinearLayout)findViewById(R.id.bidding_touch);
        biddingBut.setOnClickListener(this);
        sellHisBut = (LinearLayout)findViewById(R.id.sell_history_touch);
        sellHisBut.setOnClickListener(this);
        bidHisBut = (LinearLayout)findViewById(R.id.bid_history_touch);
        bidHisBut.setOnClickListener(this);

        prev = (TextView) findViewById(R.id.previous_transaction_page);
        prev.setOnClickListener(this);
        next = (TextView) findViewById(R.id.next_transaction_page);
        next.setOnClickListener(this);
        blank = (TextView) findViewById(R.id.blank_text_transaction_page);

        ctx = getApplicationContext();
        intent = getIntent();

        currentBut = intent.getStringExtra("currentTransactionInfoBut");
        if(currentBut == null) currentBut = "biddingBut";
        ApiKey = intent.getStringExtra("user_apiKey");

        switch(currentBut){
            case "biddingBut":
                onBiddingButClick();
                break;
            case "sellingBut":
                onSellingButClick();
                break;
            case "bidHisBut":
                onBidHisButClick();
                break;
            case "sellHisBut":
                onSellHisButClick();
                break;
            default:
                break;
        }

       //为单一列表行添加单击事件
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
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bidding_touch:
                if(currentBut.equals("biddingBut")) return;
                currentBut = "biddingBut";
                onBiddingButClick();
                break;
            case R.id.selling_touch:
                if(currentBut.equals("sellingBut")) return;
                currentBut = "sellingBut";
                onSellingButClick();
                break;
            case R.id.bid_history_touch:
                if(currentBut.equals("bidHisBut")) return;
                currentBut = "bidHisBut";
                onBidHisButClick();
                break;
            case R.id.sell_history_touch:
                if(currentBut.equals("sellHisBut")) return;
                currentBut = "sellHisBut";
                onSellHisButClick();
                break;
            default:
                break;
        }
    }

    private void onBiddingButClick(){
        sellingBut.setBackgroundColor(DARK_COLOR);
        sellHisBut.setBackgroundColor(DARK_COLOR);
        bidHisBut.setBackgroundColor(DARK_COLOR);
        biddingBut.setBackgroundColor(BRIGHT_COLOR);

        search = new GeneralSearch("0","","",ApiKey,"","1");
        search.addObserver(this);
        search.loadList();
    }

    private void onSellingButClick(){
        sellingBut.setBackgroundColor(BRIGHT_COLOR);
        sellHisBut.setBackgroundColor(DARK_COLOR);
        bidHisBut.setBackgroundColor(DARK_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        search = new GeneralSearch("0","","",ApiKey,"","0");
        search.addObserver(this);
        search.loadList();
    }

    private void onBidHisButClick(){
        sellingBut.setBackgroundColor(DARK_COLOR);
        sellHisBut.setBackgroundColor(DARK_COLOR);
        bidHisBut.setBackgroundColor(BRIGHT_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        search = new GeneralSearch("1","","",ApiKey,"","1");
        search.addObserver(this);
        search.loadList();
    }

    private void onSellHisButClick(){
        sellingBut.setBackgroundColor(DARK_COLOR);
        sellHisBut.setBackgroundColor(BRIGHT_COLOR);
        bidHisBut.setBackgroundColor(DARK_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        search = new GeneralSearch("1","","",ApiKey,"","0");
        search.addObserver(this);
        search.loadList();
    }

/*
    private ArrayList<HashMap<String, String>> testBlankInfoGenerator(){


        return new ArrayList<HashMap<String, String>>();
    }

    private ArrayList<HashMap<String, String>> testInfoGenerator(){
        ArrayList<HashMap<String, String>> goodList = new ArrayList<HashMap<String, String>>();

        // 新建一个 HashMap
        HashMap<String, String> map1 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map1.put(Utility.KEY_ID, "0");
        map1.put(Utility.KEY_NAME, "德国熊啤酒");
        map1.put(Utility.KEY_SELLER, "Paul");
        map1.put(Utility.KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428604005173.bmp");
        // HashList添加到数组列表
        goodList.add(map1);

        HashMap<String, String> map2 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map2.put(Utility.KEY_ID, "1");
        map2.put(Utility.KEY_NAME, "SONY PS4");
        map2.put(Utility.KEY_SELLER, "Tony");
        map2.put(Utility.KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428610080251.bmp");
        // HashList添加到数组列表
        goodList.add(map2);

        HashMap<String, String> map3 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map3.put(Utility.KEY_ID, "2");
        map3.put(Utility.KEY_NAME, "LG G3 Phone");
        map3.put(Utility.KEY_SELLER, "Big Brother");
        map3.put(Utility.KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428613508787.bmp");
        // HashList添加到数组列表
        goodList.add(map3);

        HashMap<String, String> map4 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map4.put(Utility.KEY_ID, "3");
        map4.put(Utility.KEY_NAME, "Thinkpad T430");
        map4.put(Utility.KEY_SELLER, "Paul");
        map4.put(Utility.KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428614319776.bmp");
        // HashList添加到数组列表
        goodList.add(map4);

        blank.setText("");
        prev.setText("Previous");
        next.setText("Next");

        return goodList;
    }

*/
    @Override
    public void update(Observable observable, Object data) {
        if(observable == search) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            LazyMyBidAdapter adapter = new LazyMyBidAdapter(this, paramList);
            search.deleteObserver(this);
            list.setAdapter(adapter);
            if(((ArrayList<HashMap<String, String>>)data).size() == 0){
                blank.setText("Sorry, no item found");
                prev.setText("");
                next.setText("");
            }else{
                blank.setText("");
                prev.setText("Previous");
                next.setText("Next");
            }
        }
    }
}