package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.scentair.scentwave.BayItemArrayAdapter.customButtonListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class TestRunActivity extends Activity implements customButtonListener {

    TestRun testRun;
    ArrayList<TestStep> testSteps = MainActivity.testSteps.getTestSteps();
    BayItem[] bayItems;
    ListView listView;
    BayItemArrayAdapter aa;
    Context context;
    ArrayList<Failure> failureList;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public Integer currentRack;
    private boolean highlightFailed = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testrun);
        context = this.getApplicationContext();

        // Initialize the non-volatile storage area
        sharedPreferences = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE);
        editor = getSharedPreferences(MainActivity.TAG_MYPREFS,Context.MODE_PRIVATE).edit();

        // Check to see if we are supposed to resume a paused/aborted run
        Bundle extras = getIntent().getExtras();
        Boolean resume=false;
        if (extras!=null) {
            resume=extras.getBoolean(MainActivity.TAG_RESUME_RUN);
        }

        // Need to make sure we pull out the calibration info before starting the test run
        // Pull the associated rack number from NVM
        currentRack = sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER,0);
        String dbAddress = sharedPreferences.getString(MainActivity.TAG_DATABASE_SERVER_ADDRESS,"");

        //Initialize this test run
        testRun = new TestRun(currentRack,dbAddress);
        // Make sure we read the test steps from the proper data structure
        testRun.maxTestSteps = testSteps.size();
        Integer savedTestStep = sharedPreferences.getInt(MainActivity.TAG_LAST_STEP_COMPLETE,0);

        if (resume) {
            // Get the step to start on from preferences
            testRun.setCurrentTestStep(savedTestStep);
        }
        else {
            // We are not resuming, start from the beginning
            savedTestStep = 1;
        }
        TestStep firstStep = testSteps.get(savedTestStep-1);
        firstStep.setStartTime();

        failureList = MainActivity.failures.getFailures();

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.list_view);
        listView.setItemsCanFocus(true);

        View footerView =  ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.baylistfooter, null, false);
        listView.addFooterView(footerView);

        Button passAllButton = (Button) findViewById(R.id.pass_all_button);
        passAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passAll();
            }
        });

        bayItems= new BayItem[testRun.numberOfBays];

        for(int i=0;i<testRun.numberOfBays;i++){
            boolean status = testRun.rack.bays[i].active;
            Integer offset = testRun.rack.bays[i].calibrationOffset;
            bayItems[i]=new BayItem(i+1,status,offset);
        }
        aa= new BayItemArrayAdapter(this, bayItems);
        aa.setCustomButtonListener(TestRunActivity.this);

        listView.setAdapter(aa);

        updateView();
    }

    @Override
    public void onPassButtonClickListener(int position, int listViewPosition) {
        // One of the test steps has passed for one of the units in the bays
        // Here is where we should:
        //   Check to see if all units have passed this test step, if so, start a new step
        //   Update the results totals
        //   change the row in some visual way (maybe change to light green background color?
        //   scroll down to the next entry

        bayItems[position].stepStatus = "Passed";
        listView.smoothScrollToPosition(position+2);

        // update results totals
        updateCounts();

    }

    @Override
    public void onFailButtonClickListener(int position, int listViewPosition) {
        // One of the test steps has failed for one of the units in the bays
        // Here is where we should:
        //   Trigger a dialog to tag the failure
        //   Check to see if all units have been tested for this step, if so, start a new step
        //   Update the results totals
        //   change the row in some visual way (maybe change to red background color?
        //   scroll down to the next entry

        // Here we need an AlertDialog that provides a list of potential failure reasons
        TestStep testStep = testSteps.get(testRun.currentTestStep-1);

        final CharSequence[] failureStrings = new CharSequence[testStep.possibleFailures.size()];

        for (int i=0;i<testStep.possibleFailures.size();i++) {
            Failure failure = failureList.get(testStep.possibleFailures.get(i));
            failureStrings[i] = failure.failureText;
        }

        bayItems[position].stepStatus = "Failed";
        bayItems[position].failStep = testRun.currentTestStep;
        listView.smoothScrollToPosition(position+2);

        final int bayPosition = position;

        LayoutInflater inflater= getLayoutInflater();
        View dialogView = (View) inflater.inflate(R.layout.failureitem,null);

        //TODO make this list prettier by using an adapter and a custom layout.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Failure Reason")
                .setItems(failureStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Load the proper failure reason into the failure field
                        bayItems[bayPosition].failCause = failureStrings[which].toString();
                        aa.notifyDataSetChanged();
                    }
                })
                .setView(dialogView)
                .show();

        updateCounts();
    }

    @Override
    public void onScentAirBarCodeClickListener(int position, int listViewPosition) {
        // Scentair barcode has been entered, need to scroll to the next row
        // skip bays that are inactive

        while (!bayItems[++position].bay.active);

        if (position<=testRun.numberOfBays) {
            // if there are bays left to scroll to, move on down
            listView.smoothScrollToPosition(position+2);

            listView.setItemsCanFocus(true);
            listView.setSelection(position);

            listView.requestFocus();
        }
    }

    private void passAll () {
        // The operator has scrolled to the end of the bay list and pressed pass all.
        // Update the data to be all 'pass' (do not flag any fails here)

        // This button should only work if there were no failed tests so far
        if (testRun.currentStepUnitsFailed == 0) {
            for (int i = 0; i < testRun.numberOfBays; i++) {
                //reset background colors
                bayItems[i].stepStatus = "Passed";
            }
        } else {
            // Highlight the failed tests
            highlightFailed=true;
        }
        updateCounts();
    }

    private Boolean loadNextStep() {
        // This is the code to load the next test step and reset the variables for a new run
        // through the bay list
        // Here we need to reset the background colors
        // move the text to the next step
        // reset the various counters
        // go back to the top of the list
        Boolean returnValue=false;

        for (int i=0;i<testRun.numberOfBays;i++) {
            //If any unit has failed this step, it will be exempt from future steps.
            if (bayItems[i].stepStatus=="Failed" || bayItems[i].stepStatus=="Failed previous step") {
                //It has failed, skip it for the rest of the run.
                bayItems[i].stepStatus = "Failed previous step";
            } else bayItems[i].stepStatus = "Not Tested";
        }

        // Update the step complete timestamp
        TestStep oldTestStep = testSteps.get(testRun.currentTestStep-1);
        oldTestStep.setEndTime();

        //reset the counters
        updateCounts();

        testRun.currentTestStep++;

        // Check if that is the end of the steps and end of this run
        if (testRun.currentTestStep>testRun.maxTestSteps)
        {
            // End of this run, report results.

            //Update NVM to show the last test run was completed
            editor.putInt(MainActivity.TAG_LAST_STEP_COMPLETE,-1);
            editor.commit();

            //Write out test results in a new task

            // Includes adding all the new serial numbers to the serial number tables
                //mitec
                //scentair
            // Includes adding the relevant test data to the tables
            //TODO

            //includes saving the results

            //close this activity
            finish();
        }
        else {
            // There is at least one more step left in this run
            returnValue=true;

            //update NVM to save state and start on next step on restart/reboot
            editor.putInt(MainActivity.TAG_LAST_STEP_COMPLETE,testRun.currentTestStep);
            editor.commit();

            //Update the step begun timestamp
            TestStep newTestStep = testSteps.get(testRun.currentTestStep-1);
            newTestStep.setStartTime();

            //scroll back to the top of the list
            listView.smoothScrollToPosition(0);
        }
        return returnValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateCounts(){
        testRun.currentStepUnitsFailed=0;
        testRun.currentStepUnitsPassed=0;
        testRun.overallUnitsFailed=0;

        // update results totals
        for (int i=0;i<testRun.numberOfBays;i++) {
            //count up the numbers of passed and failed units
            if (bayItems[i].stepStatus=="Failed") testRun.currentStepUnitsFailed++;
            if (bayItems[i].stepStatus=="Passed") testRun.currentStepUnitsPassed++;
            if (bayItems[i].stepStatus=="Failed previous step") testRun.overallUnitsFailed++;
        }

        testRun.currentStepUnitsTested = testRun.currentStepUnitsFailed+testRun.currentStepUnitsPassed+testRun.overallUnitsFailed;

        Boolean showView=true;

        if ( testRun.currentStepUnitsTested >= testRun.numberOfActiveBays ) {
            showView=loadNextStep();
        }
        if (showView) updateView();
    }

    private void updateView(){
        TestStep testStep = testSteps.get(testRun.currentTestStep-1);

        //Get the header info loaded from the data structure
        TextView currentStep = (TextView)findViewById(R.id.teststepnumber);
        String text= "Step " + testRun.currentTestStep.toString() + " of " + testRun.maxTestSteps.toString();
        currentStep.setText(text);

        //Load the current step start time
        TextView currentStepStartTime = (TextView)findViewById(R.id.start_time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd hh:mm:ss");
        String dateToStr = format.format(testStep.getStartTime());
        currentStepStartTime.setText(dateToStr);

        //Get the current progress info loaded
        TextView currentProgress = (TextView) findViewById(R.id.test_step_progress);
        text="Progress " + testRun.currentStepUnitsTested.toString() + "/" + testRun.numberOfActiveBays.toString();
        currentProgress.setText(text);

        TextView activeBays = (TextView) findViewById(R.id.active_bays);
        text= testRun.numberOfActiveBays.toString();
        activeBays.setText(text);

        TextView skippedBaysView = (TextView) findViewById(R.id.skipped_bays);
        Integer skippedBays = testRun.overallUnitsFailed;
        text= skippedBays.toString();
        skippedBaysView.setText(text);

        TextView passedView = (TextView) findViewById(R.id.number_passed);
        text= testRun.currentStepUnitsPassed.toString();
        passedView.setText(text);

        TextView failedView = (TextView) findViewById(R.id.number_failed);
        text= testRun.currentStepUnitsFailed.toString();
        failedView.setText(text);
        if (highlightFailed){
            failedView.setBackgroundColor(Color.YELLOW);
            highlightFailed=false;
        } else {
            failedView.setBackgroundColor(Color.WHITE);
        }

        //Get the verify list loaded
        TextView verifyText = (TextView) findViewById(R.id.teststepverifylist);
        text = testStep.expectedResults;
        verifyText.setText(text);

        //Get the test step information from the Test Steps list
        TextView step1Info = (TextView) findViewById(R.id.teststepinstruction1);
        text = testStep.testStep1;
        step1Info.setText(text);

        TextView step2Info = (TextView) findViewById(R.id.teststepinstruction2);
        text = testStep.testStep2;
        step2Info.setText(text);

        aa.notifyDataSetChanged();
    }
}
