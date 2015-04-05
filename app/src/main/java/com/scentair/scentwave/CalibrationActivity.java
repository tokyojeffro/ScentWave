package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.scentair.scentwave.CalibrationBayArrayAdapter.customCalibrationButtonListener;

public class CalibrationActivity extends Activity implements customCalibrationButtonListener {

    Rack rack;
    ListView listView;
    CalibrationBayArrayAdapter aa;
    Context context;
    Button toggleRackButton;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public Integer currentRack;
    static Integer[] phidgets = new Integer[3];

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration);
        context = this.getApplicationContext();

        // Initialize the non-volatile storage area
        sharedPreferences = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE);

        // Pull the associated rack number from NVM
        currentRack = sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER,0);

        // Pull the three associated phidget serial numbers from NVM
        // These (as well as the calibration data) will be saved to the server

        phidgets[0] = sharedPreferences.getInt(MainActivity.TAG_PHIDGET_1,0);
        phidgets[1] = sharedPreferences.getInt(MainActivity.TAG_PHIDGET_2,0);
        phidgets[2] = sharedPreferences.getInt(MainActivity.TAG_PHIDGET_3,0);

        //Initialize the rack
        rack = new Rack(currentRack,phidgets);

        // Used in case we change the rack or the serial numbers here in calibration
        editor = getSharedPreferences(MainActivity.TAG_MYPREFS,Context.MODE_PRIVATE).edit();

        toggleRackButton = (Button)findViewById(R.id.current_rack);
        toggleRackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRack==1) {
                    currentRack = 2;
                } else {
                    currentRack = 1;
                }
                // Save in NVM
                editor.putInt(MainActivity.TAG_RACK_NUMBER,currentRack);
                editor.commit();

                updateView();
            }
        });

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.calibrate_list_view);

        aa = new CalibrationBayArrayAdapter(this, rack.getBays());

        aa.setCustomCalibrationButtonListener(CalibrationActivity.this);

        listView.setAdapter(aa);
        updateView();
    }

    @Override
    public void onToggleButtonClickListener(int position, int listViewPosition) {
        // Toggle the active status of this bay

        if (rack.bays[position].active) {
            // Set inactive
            rack.bays[position].active = false;
        }
        else rack.bays[position].active = true;

        updateView();
    }

    @Override
    public void onCalibrationIncrementButtonClickListener(int position, int listViewPosition) {
        // Increment the calibration offset value
        rack.bays[position].calibrationOffset++;

        updateView();
    }

    @Override
    public void onCalibrationDecrementButtonClickListener(int position, int listViewPosition) {
        // Increment the calibration offset value
        rack.bays[position].calibrationOffset--;

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateView(){
        //TestStep testStep = testSteps.get(testRun.currentTestStep-1);

        //Get the header info loaded from the data structure

        String text= currentRack.toString();
        toggleRackButton.setText(text);

        // Set up phidget serial number 1
        TextView phidgetSerialView = (TextView) findViewById(R.id.phidget_1);
        text="unset1";
        phidgetSerialView.setText(text);


        // Set up phidget serial number 2
        phidgetSerialView = (TextView) findViewById(R.id.phidget_2);
        text="unset2";
        phidgetSerialView.setText(text);

        // Set up phidget serial number 3
        phidgetSerialView = (TextView) findViewById(R.id.phidget_3);
        text="unset3";
        phidgetSerialView.setText(text);

        aa.notifyDataSetChanged();
    }
}


