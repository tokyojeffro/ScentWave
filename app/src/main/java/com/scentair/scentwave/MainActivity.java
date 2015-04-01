package com.scentair.scentwave;

import com.scentair.scentwave.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.content.*;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    // Need some global variables to store array values
    public static Operators operators;
    public static TestSteps testSteps;
    public static Failures failures;

    // These are the tags for the data stored in NVM as preferences
    public static final String MyPreferences = "ScentwavePrefs";
    public static final String OPERATOR_NAME = "OPERATOR_NAME";
    public static final String CURRENT_TEST_STATUS = "CURRENT_TEST_STATUS";
    public static final String LAST_STEP_COMPLETE = "LAST_STEP_COMPLETE";

    SharedPreferences sharedPreferences;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Load up shared prefs and populate fields
        updateView();

        //start initialization from the DB in an asynchronous task
        new loadDBValues().execute("http://this is a test");
    }

    // layout button triggers this method to start new activity
    public void startCalibrate (View view) {
        //TODO add method when activity is ready
    }

    // layout button triggers this method to start new activity
    public void startTestRun (View view) {
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
        Intent intent = new Intent(this,ChangeOperatorActivity.class);
        startActivity(intent);
    }

    private class loadDBValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            operators = new Operators();
            testSteps = new TestSteps();
            failures = new Failures();
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
        sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        String currentOperator = sharedPreferences.getString(OPERATOR_NAME,null);
        if (currentOperator==null) {
            currentOperator="not set";
        }
        TextView textView = (TextView) findViewById(R.id.current_operator);
        textView.setText(currentOperator);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}