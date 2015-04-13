package com.scentair.scentwave;

import com.scentair.scentwave.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.content.*;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    // Need some global variables to store array values
    public static Operators operators;
    public static TestSteps testSteps;
    public static Failures failures;
    public static Rack rack;
    private Boolean resumeRun = false;

    // These are the tags for the data stored in NVM as preferences
    public static final String TAG_MYPREFS = "ScentwavePrefs";
    public static final String TAG_OPERATOR_NAME = "OPERATOR_NAME";
    public static final String TAG_RESUME_AVAILABLE = "RESUME_AVAILABLE";
    public static final String TAG_RACK_NUMBER = "RACK_NUMBER";
    public static final String TAG_DATABASE_SERVER_ADDRESS="DATABASE_SERVER_ADDRESS";
    public static final String TAG_PHIDGET_SERVER_ADDRESS="PHIDGET_SERVER_ADDRESS";

    SharedPreferences sharedPreferences;
    public static String phidgetServerAddress;
    public static String dbServerAddress;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        sharedPreferences=getSharedPreferences(TAG_MYPREFS, Context.MODE_PRIVATE);
        phidgetServerAddress = sharedPreferences.getString(TAG_PHIDGET_SERVER_ADDRESS,"192.168.1.22");
        dbServerAddress = sharedPreferences.getString(TAG_DATABASE_SERVER_ADDRESS,"192.168.1.26");
        resumeRun = sharedPreferences.getBoolean(TAG_RESUME_AVAILABLE,false);

        // Load up shared prefs and populate fields
        updateView();

        //start initialization from the DB in an asynchronous task
        new loadDBValues().execute("http://this is a test");
    }

    // layout button triggers this method to start new activity
    public void startCalibrate (View view) {
        Intent intent = new Intent(this,CalibrationActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startTestRun (View view) {
        Intent intent = new Intent(this,TestRunActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    // This button is hidden unless a resume is possible
    public void resumeTestRun (View view) {
        Intent intent = new Intent(this,TestRunActivity.class);
        intent.putExtra(TAG_RESUME_AVAILABLE, true);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startMonitor (View view) {
        Intent intent = new Intent(this,MonitorActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startChangeOperator (View view) {
        Intent intent = new Intent(this,ChangeOperatorActivity.class);
        startActivity(intent);
    }

    // layout button triggers this method to start new activity
    public void startChangePreferences (View view) {
        Intent intent = new Intent(this,PreferencesActivity.class);
        startActivity(intent);
    }

    private class loadDBValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            operators = new Operators(dbServerAddress);
            testSteps = new TestSteps(dbServerAddress);
            failures = new Failures(dbServerAddress);
            Integer currentRack=sharedPreferences.getInt(TAG_RACK_NUMBER,1);
            rack = new Rack(currentRack,dbServerAddress);

            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
          //do nothing for now
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if this is what happens
        updateView();
    }

    private void updateView(){
        //Load up the shared preferences
        String currentOperator = sharedPreferences.getString(TAG_OPERATOR_NAME,null);
        if (currentOperator==null) {
            currentOperator="not set";
        }
        TextView textView = (TextView) findViewById(R.id.current_operator);
        textView.setText(currentOperator);

        resumeRun = sharedPreferences.getBoolean(TAG_RESUME_AVAILABLE,false);
        if (resumeRun) {
            // The tests were not completed, offer the option to start at the last step completed.
            Button button = (Button) findViewById(R.id.ResumeTestButton);
            button.setVisibility(Button.VISIBLE);
        }
        else {
            Button button = (Button) findViewById(R.id.ResumeTestButton);
            button.setVisibility(Button.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}