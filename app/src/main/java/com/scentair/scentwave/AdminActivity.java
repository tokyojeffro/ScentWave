package com.scentair.scentwave;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.content.*;

public class AdminActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
    }

    // layout button triggers this method to start new activity
    public void startCalibrate (View view) {
        Intent intent = new Intent(this,CalibrationActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startMonitor (View view) {
        Intent intent = new Intent(this,MonitorActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startChangePreferences (View view) {
        Intent intent = new Intent(this,PreferencesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}