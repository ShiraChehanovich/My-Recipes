package com.shirac.myrecipes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private Button btnSave;
    private EditText edtTime;
    private Switch switchLongTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("file", Context.MODE_PRIVATE);
        btnSave = findViewById(R.id.btnSave);
        edtTime = findViewById(R.id.edtTime);
        switchLongTime = findViewById(R.id.switchLongTime);

        boolean isLongTimeEnabled = sp.getBoolean("longTimeEnabled", false);
        int longTimeValue = sp.getInt("longTimeValue", 0);

        switchLongTime.setChecked(isLongTimeEnabled);
        edtTime.setText(String.valueOf(longTimeValue));
        edtTime.setEnabled(!isLongTimeEnabled);
        if (isLongTimeEnabled) {
            edtTime.setText("0");
        }

        switchLongTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtTime.setEnabled(!isChecked);
            if (isChecked) {
                edtTime.setText("0");
            }
        });

        btnSave.setOnClickListener(v -> {
            int time = Integer.parseInt(edtTime.getText().toString());
            sp.edit()
                    .putBoolean("longTimeEnabled", switchLongTime.isChecked())
                    .putInt("longTimeValue", time)
                    .apply();
            Intent intent = new Intent(SettingsActivity.this, ChooseActivity.class);
            startActivity(intent);
        });
    }
}

