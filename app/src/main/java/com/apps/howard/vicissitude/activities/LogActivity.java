package com.apps.howard.vicissitude.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.apps.howard.vicissitude.R;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
