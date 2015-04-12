package com.project.zxt.ustauctionhouse;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;


public class SearchResult extends Activity {

    static final String KEY_ID = "id";
    static final String KEY_NAME= "name";
    static final String KEY_SELLER = "seller";
    static final String KEY_IMAGE = "image";

    private ListView list;
    private LazyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_display_frame);
        list=(ListView)findViewById(R.id.frame_list);

        ArrayList<HashMap<String, String>> goodList = new ArrayList<HashMap<String, String>>();

        // 新建一个 HashMap
        HashMap<String, String> map1 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map1.put(KEY_ID, "0");
        map1.put(KEY_NAME, "德国熊啤酒");
        map1.put(KEY_SELLER, "Paul");
        map1.put(KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428604005173.bmp");
        // HashList添加到数组列表
        goodList.add(map1);

        HashMap<String, String> map2 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map2.put(KEY_ID, "1");
        map2.put(KEY_NAME, "SONY PS4");
        map2.put(KEY_SELLER, "Tony");
        map2.put(KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428610080251.bmp");
        // HashList添加到数组列表
        goodList.add(map2);

        HashMap<String, String> map3 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map3.put(KEY_ID, "2");
        map3.put(KEY_NAME, "LG G3 Phone");
        map3.put(KEY_SELLER, "Big Brother");
        map3.put(KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428613508787.bmp");
        // HashList添加到数组列表
        goodList.add(map3);

        HashMap<String, String> map4 = new HashMap<String, String>();
        //每个子节点添加到HashMap关键= >值
        map4.put(KEY_ID, "3");
        map4.put(KEY_NAME, "Thinkpad T430");
        map4.put(KEY_SELLER, "Paul");
        map4.put(KEY_IMAGE, Utility.serverUrl + "/portrait/" + "1428614319776.bmp");
        // HashList添加到数组列表
        goodList.add(map4);

        adapter=new LazyAdapter(this, goodList);
        list.setAdapter(adapter);


        //为单一列表行添加单击事件
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //这里可以自由发挥，比如播放一首歌曲等等
                Log.i("onClickEntry: ", "Position is " + position);
                Log.i("onClickEntry: ", "ID is " + id);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
