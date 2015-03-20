package com.scentair.scentwave;

import com.scentair.scentwave.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.content.*;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }

    // layout button triggers this method to start new activity
    public void startCalibrate (View view) {
        //TODO add method when activity is ready
    }

    // layout button triggers this method to start new activity
    public void startTestRun (View view) {
        //TODO add method when activity is ready
        Intent intent = new Intent(this,TestRunActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startMonitor (View view) {
        Intent intent = new Intent(this,MonitorActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startChangeOperator (View view) {
        //TODO add method when activity is ready
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}