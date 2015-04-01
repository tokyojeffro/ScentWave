package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testrun);
        context = this.getApplicationContext();

        //Initialize this test run
        //TODO Make sure this input is the rack number from the calibration results
        testRun = new TestRun(1);
        // Make sure we read the test steps from the proper data structure
        testRun.maxTestSteps = testSteps.size();
        TestStep firstStep = testSteps.get(0);
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
            bayItems[i]=new BayItem(i+1,"","","Unplugged","",0);
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
        testRun.currentStepUnitsPassed++;
        testRun.currentStepUnitsTested++;

        if ( testRun.currentStepUnitsTested >= testRun.numberOfBays ) {
            loadNextStep();
        }
        updateView();
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
                .show();

        // update results totals
        testRun.currentStepUnitsFailed++;
        testRun.currentStepUnitsTested++;

        if ( testRun.currentStepUnitsTested > testRun.numberOfBays )
        {
            loadNextStep();
        }

        updateView();
    }

    private void passAll () {
        // The operator has scrolled to the end of the bay list and pressed pass all.
        // Update the data to be all 'pass' (do not flag any fails here)
        for (int i=0;i<testRun.numberOfBays;i++) {
            //reset background colors
            bayItems[i].stepStatus = "Passed";
        }
        loadNextStep();

        updateView();
    }

    private void loadNextStep() {
        // This is the code to load the next test step and reset the variables for a new run
        // through the bay list
        // Here we need to reset the background colors
        // move the text to the next step
        // reset the various counters
        // go back to the top of the list
        for (int i=0;i<testRun.numberOfBays;i++) {
            //reset background colors
            bayItems[i].stepStatus = "Not Tested";
        }

        // Update the step complete timestamp
        TestStep oldTestStep = testSteps.get(testRun.currentTestStep-1);
        oldTestStep.setEndTime();

        //reset the counters
        testRun.currentStepUnitsPassed = 0;
        testRun.currentStepUnitsTested = 0;
        testRun.currentStepUnitsFailed = 0;

        testRun.currentTestStep++;

        //Update the step begun timestamp
        TestStep newTestStep = testSteps.get(testRun.currentTestStep-1);
        newTestStep.setStartTime();

        //scroll back to the top of the list
        listView.smoothScrollToPosition(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        TextView currentProgress = (TextView) findViewById(R.id.test_step_progess);
        text="Progress " + testRun.currentStepUnitsTested.toString() + "/" + testRun.numberOfBays.toString();
        currentProgress.setText(text);

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
