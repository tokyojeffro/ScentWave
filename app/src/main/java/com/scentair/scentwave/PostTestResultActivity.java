package com.scentair.scentwave;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostTestResultActivity extends Activity {
    private TestRun testRun;
    EditText comments;
    Integer numberOfBays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_results);

        // Need to load the referenced test run from Extras
        Gson gson = new Gson();
        Intent intent = getIntent();
        String jsonTestRun = intent.getStringExtra("TestRun");
        numberOfBays = intent.getIntExtra("RackBays",0);
        // Unpack JSON into the new local variable for the test run
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

        comments = (EditText) findViewById(R.id.results_comments);
    }
    // layout button triggers this method to start new activity
    public void postResults (View view) {
        testRun.testResult.comments = comments.getText().toString();
        new saveTestResults().execute("This text is necessary, but useless");
        finish();
    }

    // layout button triggers this method to start new activity
    public void cancelResults (View view) {
        //TODO probably need a double check popup to make sure
        finish();
    }

    private class saveTestResults extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            testRun.saveTestResults(MainActivity.dbServerAddress,numberOfBays);

            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
            //do nothing for now
        }
    }
}