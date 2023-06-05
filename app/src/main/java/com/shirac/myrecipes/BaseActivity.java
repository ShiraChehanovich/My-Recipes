package com.shirac.myrecipes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class BaseActivity extends AppCompatActivity {
    private BatteryReceiver batteryReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // =============== Battery Change Broadcast Receiver ===============
        // create the Broadcast Receiver
        batteryReceiver = new BatteryReceiver();
        // Define the IntentFilter.
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // register the receiver & filter
        registerReceiver(batteryReceiver, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // unregister the receiver
        unregisterReceiver(batteryReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.about_settings:
                openDialogAbout();
                break;
            case R.id.exit_settings:
                openDialogExit();
                break;
        }
        return true;
    }

    private void openDialogAbout(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(BaseActivity.this);
        View mView  = getLayoutInflater().inflate(R.layout.dialog_about,null);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void openDialogExit(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(BaseActivity.this);
        View mView  = getLayoutInflater().inflate(R.layout.dialog_exit,null);
        TextView mExit = (TextView) mView.findViewById(R.id.txtYesId);
        TextView mNotExit = (TextView)mView.findViewById(R.id.txtNoId);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Error", "before killing process");
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                finish();
            }
        });
        mNotExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

    }

}
