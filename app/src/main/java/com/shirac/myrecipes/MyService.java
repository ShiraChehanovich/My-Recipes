package com.shirac.myrecipes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
//import android.support.v4.app.NotificationCompat;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MyService extends Service {


    private int i=0;
    private ArrayList<Integer> minutes;
    private ArrayList<String> tasks;
    private static String CHANNEL1_ID = "channel1";
    private static String CHANNEL1_NAME = "Channel 1 Demo";
    private static int id = 1;
    private NotificationManager notificationManager;

    private SharedPreferences sp;//to get recipe name and minutes for timer from recipe class

    public class LocalBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        //lists to save info for all the different notifications on the one channel
        minutes =new ArrayList<>();
        tasks = new ArrayList<>();

        sp = getSharedPreferences("file", Context.MODE_PRIVATE);


        // 1. Get reference to Notification Manager
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 2. Create Notification Channel (ONLY ONEs)
        //Create channel only if it is not already created
        if (notificationManager.getNotificationChannel(CHANNEL1_ID) == null)
        {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL1_ID,
                    CHANNEL1_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(notificationChannel);

        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        minutes.add(sp.getInt("numMinutes", 0));
        tasks.add(sp.getString("nameTask", "task"));

        // The service is starting, due to a call to startService()
        i=0;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(i<minutes.get(id-1)) {
                    SystemClock.sleep(60000);//sleep for num minutes
                    i++;
                }
                showNotification();

            }
        }).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 3. When buCreate the Notification & send it to the device status bar
    private void showNotification()
    {
        Log.d("debug", "Timer done!");
        String notificationTitle = "My Recipes";
        String notificationText = "Done with " + tasks.get(id-1) +" id= "+id;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        // Build Notification with NotificationCompat.Builder
        // on Build.VERSION < Oreo the notification avoid the CHANEL_ID
        Notification notification = new NotificationCompat.Builder(this, CHANNEL1_ID)
                .setSmallIcon(R.drawable.vicon)  //Set the icon
                .setContentTitle(notificationTitle)         //Set the title of Notification
                .setContentText(notificationText)           //Set the text for notification
                .setContentIntent(pendingIntent)
                .build();
        // Send the notification to the device Status bar.
        notificationManager.notify(id, notification);

        // Start foreground service.
        startForeground(id, notification);

        id++;  // for multiple notifications on the same chanel
    }
}

