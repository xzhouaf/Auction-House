package com.project.zxt.ustauctionhouse.ViewItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.project.zxt.ustauctionhouse.GCM.RegisterApp;
import com.project.zxt.ustauctionhouse.ItemListView.ImageLoader;
import com.project.zxt.ustauctionhouse.LoginRelated.Register;
import com.project.zxt.ustauctionhouse.R;
import com.project.zxt.ustauctionhouse.Utility.Utility;
import com.project.zxt.ustauctionhouse.bottomMenu.NewItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Paul on 2015/4/14.
 *
 */
public class ViewItem extends Activity implements View.OnClickListener {

    private static final String TAG = "View Item";
    private Intent intent;
    private Context ctx;
    private ImageView image, bidNow;
    private ImageLoader imageLoader;
    private TextView timeLeft, viewSeller;
    private UpdateTimeLeft timeUpdater;
    private boolean continueUpdate = true;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_item);
        Log.i(TAG, "Activity: onCreate()");

        intent = getIntent();
        ctx = getApplicationContext();

        image = (ImageView) findViewById(R.id.ViewItemImage);
        timeLeft = (TextView) findViewById(R.id.ViewTimeLeft);
        viewSeller = (TextView) findViewById(R.id.ViewSeller);
        viewSeller.setOnClickListener(this);
        bidNow = (ImageView) findViewById(R.id.bidNow);
        bidNow.setOnClickListener(this);
        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        String imageFileURL = intent.getStringExtra(Utility.KEY_IMAGE);
        imageLoader = new ImageLoader(ctx);
        imageLoader.DisplayImage(imageFileURL, image);

        timeUpdater = new UpdateTimeLeft();
        timeUpdater.execute();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        continueUpdate = false;
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bidNow:
                onBidNowClick();
                break;
            case R.id.ViewSeller:
                Intent intent = new Intent(ctx, ViewSeller.class);
                startActivity(intent);
            default:
                break;
        }
    }

    private void onBidNowClick(){
        final EditText userInputPrice = new EditText(this);
        userInputPrice.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        AlertDialog a = new AlertDialog.Builder(this)
                .setTitle("Input your bid price here")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(userInputPrice)
                .setPositiveButton("Bid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                        confirmBid(userInputPrice.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(userInputPrice.getWindowToken(), 0);
                    }
                })
                .show();

        Timer timer = new Timer();
        timer.schedule(
                new TimerTask(){
                    @Override
                    public void run(){
                        imm.showSoftInput(userInputPrice, InputMethodManager.HIDE_NOT_ALWAYS);
                        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }, 50);
    }

    private void confirmBid(String input){
        if(!validateInputPrice(input)){
            new AlertDialog.Builder(this)
                    .setTitle("Information")
                    .setMessage("You must input a valid price")
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBidNowClick();
                        }
                    })
                    .show();
            return;
        }
    }

    private boolean validateInputPrice(String in){
        //TODO: validate input price
        if(in.equals("")) return false;
        return true;
    }

    private class UpdateTimeLeft extends AsyncTask<Object, String, Boolean> {

        private int counter = 0;

        @Override
        protected void onProgressUpdate(String... values) {
            timeLeft.setText(counter+"");
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            while(continueUpdate) {
                counter++;
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Stopped! ", "");
            return null;
        }
    }

}