package com.project.zxt.ustauctionhouse.LiveAuction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.zxt.ustauctionhouse.NewListView.RefreshListView;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.LiveUnit;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnection;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketConnectionHandler;
import com.project.zxt.ustauctionhouse.WebSocket.WebSocketException;
import com.project.zxt.ustauctionhouse.bottomMenu.BottomMenuHome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 15年5月11日.
 */
public class LiveListFragment extends Fragment implements BottomMenuHome.OnPassParamListener,
        View.OnClickListener, RefreshListView.OnRefreshListener, RefreshListView.OnLoadListener {

    private static final String TAG = "Live List";
    private String UserName, Email, ApiKey, UserID;
    private Intent intent;
    private Context ctx;
    private RefreshListView refreshLv;
    private TextView goBackToTop;
    private long mBackToTopTime;
    public WebSocketConnection wsC = new WebSocketConnection();
    private ArrayList<HashMap<String, String>> goodList;
    private LiveListAdapter adapter;
    private ArrayList<HashMap<String, String>> dataToDisplay = new ArrayList<>();
    private int lastLoad = 0;
    private boolean isToEnd = false;
    boolean updated = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        Log.i(TAG, "Fragment View: onCreate()");

        View rootView = inflater.inflate(R.layout.live_list, container, false);
        ctx = getActivity().getApplicationContext();
        intent = getActivity().getIntent();

        //goBackToTop = (TextView) rootView.findViewById(R.id.live_list_frame_title);
        //goBackToTop.setOnClickListener(this);


        refreshLv = (RefreshListView) rootView.findViewById(R.id.livebid_listview);
        refreshLv.setOnRefreshListener(this);
        refreshLv.setOnLoadListener(this);
        refreshLv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.i("See why bug: ", position + "");
                        if (position > goodList.size() || position <= 0) return;
                        Intent intent = new Intent(ctx, WebSocketLive.class);
                        intent.putExtra("room_id", goodList.get(position - 1).get(Utility.KEY_ID));
                        intent.putExtra("image", goodList.get(position - 1).get(Utility.KEY_IMAGE));
                        intent.putExtra("user_name", UserName);
                        intent.putExtra("user_ID", UserID);
                        intent.putExtra("user_apiKey", ApiKey);
                        startActivity(intent);
                    }
                }
        );

        return rootView;
    }

    private void wsGetRoomStart()
    {
        /*
            BasicNameValuePair pair1 = new BasicNameValuePair("Authorization", ApiKey);
            List<BasicNameValuePair> pairList = new ArrayList<>();
            pairList.add(pair1);
            */
        try {
            wsC.connect( Utility.wsUrl, null, new WebSocketConnectionHandler()
            {
                @Override
                public void onOpen()
                {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", "request_rooms");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    wsC.sendTextMessage(obj.toString());
                }

                @Override
                public void onTextMessage( String payload ){

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(payload);
                        switch(obj.getString("type")){
                            case "ping":
                                JSONObject obj_send = new JSONObject();
                                try {
                                    obj_send.put("type", "pong");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                wsC.sendTextMessage(obj_send.toString());
                                break;
                            case "say":
                                String roomList = obj.getString("list");
                                Log.i("Important:   ", roomList);
                                List<LiveUnit> unitList = Utility.string2liveunit(roomList);
                                goodList = new ArrayList<HashMap<String, String>>();
                                if(unitList != null) {
                                    for (int i = 0; i < unitList.size(); i++) {
                                        // 新建一个 HashMap
                                        HashMap<String, String> map1 = new HashMap<String, String>();
                                        //每个子节点添加到HashMap 密匙 => 值
                                        map1.put(Utility.KEY_ID, unitList.get(i).id + "");
                                        map1.put(Utility.KEY_NAME, unitList.get(i).name);
                                        map1.put(Utility.KEY_SELLER, unitList.get(i).seller_name);
                                        map1.put(Utility.KEY_CURRENT_PRICE, "$" + unitList.get(i).curr_price);
                                        map1.put(Utility.KEY_IMAGE, Utility.serverUrlForPicture + "/uploads/" + unitList.get(i).image);
                                        map1.put(Utility.KEY_LIVE_STATUS, unitList.get(i).status + "");
                                        // HashList添加到数组列表
                                        goodList.add(map1);
                                    }
                                }
                                Collections.reverse(goodList);
                                prepareDataForDisplay(true);
                                adapter = new LiveListAdapter(getActivity(), dataToDisplay);
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

                                JSONObject obj_send1 = new JSONObject();
                                try {
                                    obj_send1 .put("type", "close_client");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                wsC.sendTextMessage(obj_send1.toString());
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose( int code, String reason ){
                    //Toast.makeText(ctx, "Item posted to live!", Toast.LENGTH_SHORT).show();
                }
            } );
        } catch ( WebSocketException e ) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    //private Toast goBackTopToast = null;
    public void onClick(View v) {
        switch (v.getId()) {
            /*
            case R.id.live_list_frame_title:
                if((System.currentTimeMillis() - mBackToTopTime)>500){
                    //goBackTopToast = Toast.makeText(ctx, "Double click back to top", Toast.LENGTH_SHORT);
                    //goBackTopToast.setGravity(Gravity.TOP, 0, 150);
                    //goBackTopToast.show();
                    mBackToTopTime = System.currentTimeMillis();
                }else{
                    //goBackTopToast.cancel();
                    refreshLv.smoothScrollToPosition(0);
                    isRefreshing = true;
                    wsGetRoomStart();
                }
                break;
                */
            case R.id.livebid_status_layout:
            default:
                break;
        }
    }

    private boolean prepareDataForDisplay(boolean isRefresh) {
        if (isRefresh) {
            dataToDisplay.clear();
            lastLoad = 0;
            isToEnd = false;
        }
        if(isToEnd) return false;

        for (int i = 0; i < 5; i++) {
            if (lastLoad + 1 > goodList.size()) {
                isToEnd = true;
                return false;
            }
            dataToDisplay.add(goodList.get(lastLoad));
            lastLoad++;
        }
        return true;
    }

    private void refreshLoadData() {
        wsGetRoomStart();
        refreshLv.loadComplete(false);
    }

    private void augmentLoadData() {
        prepareDataForDisplay(false);
        if (isToEnd) {
            refreshLv.loadComplete(true);
            return;
        }

        adapter.updateView(dataToDisplay);
        if(dataToDisplay.size() == goodList.size())
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

    @Override
    public void onPassAction(String a, String b, String c, String d) {
        UserName = a;
        Email = b;
        ApiKey = c;
        UserID = d;
    }

    @Override
    public void onUpdateAction() {
        if(!updated) {
            updated = true;
            isRefreshing = true;
            wsGetRoomStart();
        }
    }

    @Override
    public void activityResultHandle(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onDoubleClick() {
        refreshLv.smoothScrollToPosition(0);
        isRefreshing = true;
        wsGetRoomStart();
    }
}
