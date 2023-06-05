package com.shirac.myrecipes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class BatteryReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int level = intent.getIntExtra("level",0);
        if(level == 15 || level == 10) {
            Toast.makeText(context, "battery level low! "+level+"% charge phone to continue with the cooking!", Toast.LENGTH_SHORT).show();
        }
        if(level < 5) {
            Toast.makeText(context, "battery level extremely low! " +level+"% charge phone to continue with the cooking!", Toast.LENGTH_SHORT).show();
        }
    }
}
