package com.scentair.scentwave;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.phidgets.*;
import com.phidgets.Phidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostTestResultActivity extends Activity {
    private TestRun testRun;
    EditText comments;
    Integer numberOfBays;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private Rack rack;
    private String phidgetServerAddress;
    private TextView[] reworkBays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_results);
        editor = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE);
        phidgetServerAddress = sharedPreferences.getString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS, "192.168.1.22");
        reworkBays = new TextView[24];
        // Link the rework bays to the array of text views
        reworkBays[0] = (TextView) findViewById(R.id.rework_bay_1);
        reworkBays[1] = (TextView) findViewById(R.id.rework_bay_2);
        reworkBays[2] = (TextView) findViewById(R.id.rework_bay_3);
        reworkBays[3] = (TextView) findViewById(R.id.rework_bay_4);
        reworkBays[4] = (TextView) findViewById(R.id.rework_bay_5);
        reworkBays[5] = (TextView) findViewById(R.id.rework_bay_6);
        reworkBays[6] = (TextView) findViewById(R.id.rework_bay_7);
        reworkBays[7] = (TextView) findViewById(R.id.rework_bay_8);
        reworkBays[8] = (TextView) findViewById(R.id.rework_bay_9);
        reworkBays[9] = (TextView) findViewById(R.id.rework_bay_10);
        reworkBays[10] = (TextView) findViewById(R.id.rework_bay_11);
        reworkBays[11] = (TextView) findViewById(R.id.rework_bay_12);
        reworkBays[12] = (TextView) findViewById(R.id.rework_bay_13);
        reworkBays[13] = (TextView) findViewById(R.id.rework_bay_14);
        reworkBays[14] = (TextView) findViewById(R.id.rework_bay_15);
        reworkBays[15] = (TextView) findViewById(R.id.rework_bay_16);
        reworkBays[16] = (TextView) findViewById(R.id.rework_bay_17);
        reworkBays[17] = (TextView) findViewById(R.id.rework_bay_18);
        reworkBays[18] = (TextView) findViewById(R.id.rework_bay_19);
        reworkBays[19] = (TextView) findViewById(R.id.rework_bay_20);
        reworkBays[20] = (TextView) findViewById(R.id.rework_bay_21);
        reworkBays[21] = (TextView) findViewById(R.id.rework_bay_22);
        reworkBays[22] = (TextView) findViewById(R.id.rework_bay_23);
        reworkBays[23] = (TextView) findViewById(R.id.rework_bay_24);
        // Need to load the referenced test run from Extras
        Intent intent = getIntent();
        String jsonTestRun = intent.getStringExtra("TestRun");
        numberOfBays = intent.getIntExtra("RackBays",0);
        // Unpack JSON into the new local variable for the test run
        Gson gson = new Gson();
        testRun = gson.fromJson(jsonTestRun,TestRun.class);
        // Populate the data fields in the activity
        TextView operator = (TextView) findViewById(R.id.results_operator_name);
        operator.setText(testRun.testResult.operator);
        TextView activeBays = (TextView) findViewById(R.id.results_active_bays);
        activeBays.setText(testRun.testResult.numberOfActiveBays.toString());
        TextView passed = (TextView) findViewById(R.id.results_passed);
        passed.setText(testRun.testResult.numberOfUnitsPassed.toString());
        TextView failed = (TextView) findViewById(R.id.results_failed);
        failed.setText(testRun.testResult.numberOfUnitsFailed.toString());
        //Load the run start time
        TextView testStartTime = (TextView) findViewById(R.id.results_start_time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd hh:mm:ss", Locale.US);
        String dateToStr = format.format(testRun.testResult.step1Start);
        testStartTime.setText(dateToStr);
        //Load the run end time
        TextView testStopTime = (TextView) findViewById(R.id.results_stop_time);
        dateToStr = format.format(testRun.testResult.step5Stop);
        testStopTime.setText(dateToStr);
        //Calculate and load duration
        // This is the difference in time in milliseconds
        Long difference = testRun.testResult.step5Stop.getTime() - testRun.testResult.step1Start.getTime();
        Date duration = new Date();
        duration.setTime(difference);
        SimpleDateFormat durationFormat = new SimpleDateFormat("mm:ss",Locale.US);
        String durationToStr = durationFormat.format(duration);
        TextView durationView = (TextView) findViewById(R.id.results_duration);
        durationView.setText(durationToStr);
        // Loop through the bays to tag any failed units and set the rework array
        for (int i=0;i<rack.numberOfBays;i++) {
            if (testRun.bayItems[i].isActive && testRun.bayItems[i].isFailed) {
                reworkBays[i].setBackgroundColor(Color.RED);
            }
        }
        comments = (EditText) findViewById(R.id.results_comments);
        // Spawn a thread to load the rack DB values.
        new loadDBValues().execute("http://this string argument does nothing");
    }
    // layout button triggers this method to start new activity
    public void postResults (View view) {
        testRun.testResult.comments = comments.getText().toString();
        new saveTestResults().execute("This text is necessary, but useless");
        Toast.makeText(getApplicationContext(),"Posting Results", Toast.LENGTH_LONG).show();
    }
    // layout button triggers this method to start new activity
    public void cancelResults (View view) {
        //TODO probably need a double check popup to make sure
        finish();
    }
    private class loadDBValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Integer currentRack=sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER,1);
            rack = new Rack(currentRack,MainActivity.dbServerAddress);
            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
            // Continue setup after we have loaded the rack info from the DB.
            // add the phidget interface stuff so we can toggle LED lights
            try {
                for (int i=0;i<rack.numberOfPhidgetsPerRack;i++) {
                    rack.phidgets[i].phidget.addAttachListener(new AttachListener() {
                        public void attached(final AttachEvent ae) {
                            AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), true);
                            synchronized (handler) {
                                runOnUiThread(handler);
                                try {
                                    handler.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    rack.phidgets[i].phidget.addDetachListener(new DetachListener() {
                        public void detached(final DetachEvent ae) {
                            AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), false);
                            synchronized (handler) {
                                runOnUiThread(handler);
                                try {
                                    handler.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    rack.phidgets[i].phidget.open(rack.phidgets[i].phidgetSerialNumber, phidgetServerAddress, 5001);
                }
            } catch (PhidgetException pe) {
                pe.printStackTrace();
            }
        }
    }
    private class saveTestResults extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            testRun.saveTestResults(MainActivity.dbServerAddress);
            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),"Test Results Posted", Toast.LENGTH_LONG).show();
            //Update NVM to show the last test run was completed
            //But only after write results has completed.  May show 'Resume' upon re-entry into main activity.
            editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE,false);
            editor.putString(TestRunActivity.TAG_SAVED_TEST_RUN, "");
            editor.commit();
            finish();
        }
    }
    class AttachDetachRunnable implements Runnable {
        com.phidgets.Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(com.phidgets.Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
            // Once all 3 phidgets are attached, turn ON the bay lights where rework is required
            try {
                if (rack.phidgets[0].phidget.isAttached() &&
                        rack.phidgets[1].phidget.isAttached() &&
                        rack.phidgets[2].phidget.isAttached()) {
                        for (int i = 0; i < 8; i++) {
                            updateLED(i,testRun.bayItems[i].isFailed);
                            updateLED(i+8,testRun.bayItems[i+8].isFailed);
                            updateLED(i+16,testRun.bayItems[i+16].isFailed);
                        }
                }
            } catch (PhidgetException pe) {
                pe.printStackTrace();
            }
        }
        public void run() {
            synchronized(this)
            {
                this.notify();
            }
        }
    }
    private void updateLED (Integer bayNumber, Boolean turnOn) {
        // This function figures out the correct phidget and offset, then sets the toggle value
        Integer phidgetOffset = bayNumber/8;
        Integer phidgetSensorNumber = bayNumber - phidgetOffset*8;
        Phidget thisPhidget= rack.phidgets[phidgetOffset].phidget;
        try {
            if(thisPhidget.isAttached()){
                // Perform action on clicks, depending on whether it's now checked
                rack.phidgets[phidgetOffset].phidget.setOutputState(phidgetSensorNumber,turnOn);
            }
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }
}