/**
 *  # COMP 4521    #  ZHOU Xutong       20091184          xzhouaf@connect.ust.hk
 *  # COMP 4521    #  GAO Zihou         20090130          zgao@connect.ust.hk
 */
package com.project.zxt.ustauctionhouse.GCM;



import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.project.zxt.ustauctionhouse.LoginRelated.LogIn;
import com.project.zxt.ustauctionhouse.R;

import java.util.Random;


public class GCMIntentService extends IntentService {

    public static int NOTIFICATION_ID = 1;
    private static final String TAG = "GCMIntentService";
    //NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            switch(messageType){
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification("Send error: " + extras.toString());
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Deleted messages on server: " + extras.toString());
                    break;
                // If it's a regular GCM message, do some work.
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    // This loop represents the service doing some work.
                /*
                for (int i=0; i<5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                */
                    Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                    // Post notification of received message.
                    sendNotification(extras.getString("Notice"));
                    //showNotification(extras.getString("Notice"));
                    new AsyncVibration().execute();
                    Log.i(TAG, "Received: " + extras.toString());
                    break;
                default:
                    break;
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {

        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
               // new Intent(this, LogIn.class), 0);

        //Intent appIntent = new Intent(getApplicationContext(),IncomeMessage.class);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, LogIn.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.income_message);
        rv.setTextViewText(R.id.income_message, msg);
        rv.setTextViewText(R.id.income_title, "New Message");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.brownchuizi_tiny)
                        .setContentTitle("Auction House")
                        .setContentText(msg)
                        /*
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                                */
                        .setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //mBuilder.setContent(rv);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        NOTIFICATION_ID++;
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public class AsyncVibration extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[]{0, 200, 200, 200}, -1);
            return null;
        }
    }
}