package com.project.zxt.ustauctionhouse.SearchItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.ConditionCategoryLoader;
import com.project.zxt.ustauctionhouse.bottomMenu.BottomMenuHome;

/**
 * Created by paul on 15年4月26日.
 *
 */
public class SearchFragment extends Fragment implements View.OnClickListener, BottomMenuHome.OnPassParamListener{

    private static final String TAG = "Search";
    private String UserName, Email, ApiKey, CreatedAt, UserID;
    private Intent intent;
    private Context ctx;
    private Spinner searchCategory;
    private EditText searchKeyword;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "View Fragment: onCreate()");

        intent = getActivity().getIntent();
        ctx = getActivity().getApplicationContext();
        View vw = inflater.inflate(R.layout.search, container, false);

        searchCategory = (Spinner) vw.findViewById(R.id.searchCategorySel);

        Button testToResult = (Button)vw.findViewById(R.id.searchButton);
        testToResult.setOnClickListener(this);

        searchKeyword = (EditText) vw.findViewById(R.id.searchInput);
        return vw;

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
                intent.putExtra("user_ID", UserID);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPassAction(String a, String b, String c, String d) {
        UserName = a;
        Email = b;
        ApiKey = c;
        UserID = d;
    }

    boolean updated = false;
    @Override
    public void onUpdateAction() {
        if(!updated){
            updated = true;
            new ConditionCategoryLoader(null, searchCategory, getActivity(), false).loadConditionCategory();
        }
    }

    @Override
    public void activityResultHandle(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onDoubleClick() {

    }
}