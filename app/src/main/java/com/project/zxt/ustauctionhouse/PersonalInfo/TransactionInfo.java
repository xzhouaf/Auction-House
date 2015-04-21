package com.project.zxt.ustauctionhouse.PersonalInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.ItemListView.LazyMyBidAdapter;
import com.project.zxt.ustauctionhouse.ItemListView.MyAuctionAdapter;
import com.project.zxt.ustauctionhouse.ItemListView.MyBidHistoryAdapter;
import com.project.zxt.ustauctionhouse.ItemListView.MySellHistoryAdapter;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.ItemListView.RefreshListView;
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
public class TransactionInfo extends Activity implements View.OnClickListener, Observer, RefreshListView.OnRefreshListener {

    private RefreshListView refreshLv;
    private LinearLayout sellingBut, biddingBut, sellHisBut, bidHisBut;
    private String currentBut, ApiKey, UserID;
    private Context ctx;
    private Intent intent;
    private static final int DARK_COLOR = 0xffe9e31d, BRIGHT_COLOR = 0xfffdff29;
    private TextView prev, next, blank;
    private GeneralSearch biddingSearch, sellingSearch, bidHisSearch, sellHisSearch;
    private ArrayList<HashMap<String, String>> paramList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_info_frame);
        refreshLv=(RefreshListView)findViewById(R.id.transaction_info_frame_list);
        refreshLv.setOnRefreshListener(this);


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
        UserID = intent.getStringExtra("user_ID");

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
        refreshLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //这里可以自由发挥，比如播放一首歌曲等等
                Intent intent = new Intent(ctx, ViewItem.class);
                intent.putExtra(Utility.KEY_IMAGE, paramList.get(position-1).get(Utility.KEY_IMAGE));
                intent.putExtra(Utility.KEY_ID, paramList.get(position-1).get(Utility.KEY_ID));
                intent.putExtra("user_ID", UserID);
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

        biddingSearch = new GeneralSearch("0","","",ApiKey,"","1");
        biddingSearch.addObserver(this);
        biddingSearch.loadList();
    }

    private void onSellingButClick(){
        sellingBut.setBackgroundColor(BRIGHT_COLOR);
        sellHisBut.setBackgroundColor(DARK_COLOR);
        bidHisBut.setBackgroundColor(DARK_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        sellingSearch = new GeneralSearch("0","","",ApiKey,"","0");
        sellingSearch.addObserver(this);
        sellingSearch.loadList();
    }

    private void onBidHisButClick(){
        sellingBut.setBackgroundColor(DARK_COLOR);
        sellHisBut.setBackgroundColor(DARK_COLOR);
        bidHisBut.setBackgroundColor(BRIGHT_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        bidHisSearch = new GeneralSearch("1","","",ApiKey,"","1");
        bidHisSearch.addObserver(this);
        bidHisSearch.loadList();
    }

    private void onSellHisButClick(){
        sellingBut.setBackgroundColor(DARK_COLOR);
        sellHisBut.setBackgroundColor(BRIGHT_COLOR);
        bidHisBut.setBackgroundColor(DARK_COLOR);
        biddingBut.setBackgroundColor(DARK_COLOR);

        sellHisSearch = new GeneralSearch("1","","",ApiKey,"","0");
        sellHisSearch.addObserver(this);
        sellHisSearch.loadList();
    }


    @Override
    public void update(Observable observable, Object data) {
        boolean ok = false;
        if(observable == biddingSearch) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            LazyMyBidAdapter adapter = new LazyMyBidAdapter(this, paramList, UserID);
            biddingSearch.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            ok = true;
        }
        else if(observable == sellingSearch) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            MyAuctionAdapter adapter = new MyAuctionAdapter(this, paramList);
            sellingSearch.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            ok = true;
        }
        else if(observable == bidHisSearch) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            MyBidHistoryAdapter adapter = new MyBidHistoryAdapter(this, paramList);
            bidHisSearch.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            ok = true;
        }
        else if(observable == sellHisSearch) {
            paramList = (ArrayList<HashMap<String, String>>) data;
            MySellHistoryAdapter adapter = new MySellHistoryAdapter(this, paramList);
            sellHisSearch.deleteObserver(this);
            refreshLv.setAdapter(adapter);
            ok = true;
        }
        if(ok) {
            if ((paramList).size() == 0) {
                blank.setText("Sorry, no item found");
                prev.setText("");
                next.setText("");
            } else {
                blank.setText("");
                prev.setText("Previous");
                next.setText("Next");
            }
            refreshLv.refreshComplete();
        }
    }

    private void initLoadData() {
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
        };
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                initLoadData();
            }
        }, 10);
    }
}