package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.scentair.scentwave.BayItemArrayAdapter.customButtonListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TestRunActivity extends Activity implements customButtonListener {
    public static final String TAG_SAVED_TEST_RUN="SAVED_TEST_RUN";
    private String phidgetServerAddress;
    private TestRun testRun;
    private Rack rack;
    private ArrayList<TestStep> testSteps = MainActivity.testSteps.getTestSteps();
    private ListView listView;
    private BayItemArrayAdapter aa;
    private Context context;
    private ArrayList<Failure> failureList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String currentOperator;
    private boolean highlightFailed = false;
    private Boolean resume = false;
    static public MachineStates machineStates;
    private String showCompleteStepButton = "";
    private Button completeStepButton;
    private Resources resources;
    private static int[] popUpSpeeds;
    private String lastScentairBarcode="";
    private String lastMitecBarcode="";
    // These are used to save the current state of the test run to prefs
    private String testRunSavedState;
    private Gson gson;

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
        if (extras!=null) {
            resume=extras.getBoolean(MainActivity.TAG_RESUME_AVAILABLE);
        }

        // Need to make sure we pull out the calibration info before starting the test run
        // Pull the associated rack number from NVM
        currentOperator = sharedPreferences.getString(MainActivity.TAG_OPERATOR_NAME,"");
        phidgetServerAddress = sharedPreferences.getString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS,"192.168.1.22");

        machineStates = new MachineStates();
        resources = getResources();
        popUpSpeeds= resources.getIntArray(R.array.POP_UP_FAN_SPEEDS);

        failureList = MainActivity.failures.getFailures();
        // This will start an async process to load the current rack info from the database
        new loadDBValues().execute("http://this string argument does nothing");

        //Initialize Gson object;
        gson = new Gson();
        showCompleteStepButton="";

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.list_view);
        listView.setItemsCanFocus(true);

        View footerView =  ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.baylistfooter, listView, false);
        listView.addFooterView(footerView);

        completeStepButton = (Button) findViewById(R.id.complete_step_button);
        completeStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeTestStep();
            }
        });
    }

    @Override
    public void onPassButtonClickListener(int position, int listViewPosition) {
        // One of the test steps has passed for one of the units in the bays
        // Here is where we should:
        //   Update the counts
        //   change the row in some visual way (maybe change to light green background color?
        //   scroll down to the next entry

        //Check to see if they are marking a previous failed unit as passed.
        //if so, clear the data and reset to untested.
        String passStatus = testRun.bayItems[position].isPassReady(testRun.currentTestStep);
        switch ( testRun.bayItems[position].stepStatus) {
            case "Failed":
                // Clear the previous failure cause
                testRun.bayItems[position].failCause = "";
                testRun.bayItems[position].failCauseIndex = 0;
                testRun.bayItems[position].isFailed = false;
                // Reset tested status
                testRun.bayItems[position].stepStatus = "Not Tested";
                break;
            case "Passed":
                // The bay has already been passed
                if (testRun.currentTestStep.equals(3)) {
                    // This is a special case.  Requires a dialog popup to pick a value for the fan
                    // noted visually by the operator
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.fanspeedpopup, null);

                    final CharSequence[] popUpSpeedStrings = new CharSequence[popUpSpeeds.length];

                    for (int i = 0; i < popUpSpeeds.length; i++) {
                        Integer popUpSpeed = popUpSpeeds[i];
                        popUpSpeedStrings[i] = popUpSpeed.toString();
                    }
                    final int bayPosition = position;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Fan Display Value")
                            .setItems(popUpSpeedStrings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Load the proper failure reason into the failure field
                                    testRun.bayItems[bayPosition].fanMedDisplayValue = popUpSpeedStrings[which].toString();
                                    aa.notifyDataSetChanged();
                                }
                            })
                            .setView(dialogView)
                            .show();

                }
                listView.smoothScrollToPosition(position+2);
                break;
            case "Pass":
            case "Not Tested":
                if (testRun.currentTestStep.equals(3)) {
                    // This is a special case.  Requires a dialog popup to pick a value for the fan
                    // noted visually by the operator
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.fanspeedpopup, null);

                    final CharSequence[] popUpSpeedStrings = new CharSequence[popUpSpeeds.length];
                    for (int i = 0; i < popUpSpeeds.length; i++) {
                        Integer popUpSpeed = popUpSpeeds[i];
                        popUpSpeedStrings[i] = popUpSpeed.toString();
                    }
                    final int bayPosition = position;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Fan Display Value")
                            .setItems(popUpSpeedStrings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Load the proper failure reason into the failure field
                                    testRun.bayItems[bayPosition].fanMedDisplayValue = popUpSpeedStrings[which].toString();
                                    aa.notifyDataSetChanged();
                                }
                            })
                            .setView(dialogView)
                            .show();
                }
                listView.smoothScrollToPosition(position+2);
                // Check the status of the bay.  Is it pass ready?
                if (passStatus.equals("Pass")|| (passStatus.equals("Passed"))) {
                    // They have pressed the pass button, the bay thinks it is pass ready.  Pass it.
                    testRun.bayItems[position].stepStatus = "Passed";
                    listView.smoothScrollToPosition(position+2);
                }
                break;
            case "Machine not plugged in":
            case "Fan speeds not recorded":
            case "Barcodes not entered":
            default:
                // The pass button should toggle a fail back to not tested.  If the bay has not failed,
                // do nothing.
                break;
        }
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

        if (testRun.bayItems[position].stepStatus.equals("Passed")) {
            // Toggle back to normal state
            testRun.bayItems[position].stepStatus = "Not Tested";
            testRun.bayItems[position].lcdState = "ON";
            if (testRun.currentTestStep.equals(1)) {
                // make sure both barcodes are cleared so they can be reset
                testRun.bayItems[position].mitecBarcode="";
                testRun.bayItems[position].scentairBarcode="";
            }
        } else {
        // Here we need an AlertDialog that provides a list of potential failure reasons
            TestStep testStep = testSteps.get(testRun.currentTestStep-1);
            testRun.bayItems[position].lcdState = "OFF";
            final CharSequence[] failureStrings = new CharSequence[testStep.possibleFailures.size()];
            for (int i=0;i<testStep.possibleFailures.size();i++) {
                Integer failureOffset = testStep.possibleFailures.get(i)-1;
                Failure failure = failureList.get(failureOffset);
                failureStrings[i] = failure.failureText;
            }
            testRun.bayItems[position].stepStatus = "Failed";
            testRun.bayItems[position].isFailed = true;
            testRun.bayItems[position].failStep = testRun.currentTestStep;
            final int bayPosition = position;
            LayoutInflater inflater= getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.failureitem,null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Failure Reason")
                    .setItems(failureStrings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Load the proper failure reason into the failure field
                            testRun.bayItems[bayPosition].failCause = failureStrings[which].toString();
                            testRun.bayItems[bayPosition].failCauseIndex = which;
                            aa.notifyDataSetChanged();
                            testRun.bayItems[bayPosition].failStep = testRun.currentTestStep;
                            updateCounts();
                        }
                    })
                    .setView(dialogView)
                    .show();
            listView.smoothScrollToPosition(position+2);
        }
        updateCounts();
    }

    @Override
    public void onScentAirBarCodeClickListener(int position, String candidateText) {
        // Scentair barcode has been entered, need to scroll to the next row
        // skip bays that are inactive
        Integer nextBay = -1;
        testRun.bayItems[position].isEditScentair=false;
        String mitecCheckString = resources.getString(R.string.MITEC_BARCODE_CHECK);
        String scentairCheckString = resources.getString(R.string.SCENTAIR_BARCODE_CHECK);
        // First, check to see if the barcode is a valid scentair barcode
        if (candidateText.endsWith(scentairCheckString)) {
            // Now check to make sure they have not re-entered the same scentair barcode
            if (!candidateText.equals(lastScentairBarcode)) {
                // This is a valid scentair barcode
                // Save it into the array
                // Check to see if the mitec field is also entered for this position, then move focus to the next row mitec field
                // If the mitec field is not entered, keep the focus on this row.
                testRun.bayItems[position].scentairBarcode=candidateText;
                nextBay = testRun.setNextBarcodeEditField();
                // Make sure we keep track of the last valid scentair barcode
                lastScentairBarcode=candidateText;
            } else {
                // The same barcode was entered twice in a row, keep the focus here
                testRun.bayItems[position].isEditScentair=true;
            }
        } else if (candidateText.contains(mitecCheckString)) {
            // Now check to make sure this is not a duplicate of the last mitec barcode
            if (!candidateText.equals(lastMitecBarcode)) {
                // This is a valid Mitec barcode.  Put it where it belongs and keep focus here.
                testRun.bayItems[position].mitecBarcode=candidateText;
                nextBay = testRun.setNextBarcodeEditField();
                lastMitecBarcode=candidateText;
            } else {
                // Keep it here, dupes are not allowed
                testRun.bayItems[position].isEditScentair=true;
            }
        } else {
            // This is not a valid barcode for either type.  Keep focus here
            testRun.bayItems[position].isEditScentair=true;
        }
        updateCounts();
        if (!nextBay.equals(-1)) {
            listView.setSelection(nextBay);
        }
        aa.notifyDataSetChanged();
    }

    @Override
    public void onMitecBarCodeClickListener(int position, String candidateText) {
        // Something has been entered into the mitec field
        // validate it and enter it if is good.  then move to scentair field
        // need to move focus and cursor to scentair barcode
        Integer nextBay = -1;
        testRun.bayItems[position].isEditMitec=false;
        String mitecCheckString = resources.getString(R.string.MITEC_BARCODE_CHECK);
        String scentairCheckString = resources.getString(R.string.SCENTAIR_BARCODE_CHECK);
        // First, check to see if the barcode is a valid scentair barcode
        if (candidateText.contains(mitecCheckString)) {
            // Check for duplicate entry
            if (!candidateText.equals(lastMitecBarcode)) {
                // This is a valid mitec barcode
                // Save it into the array
                // Check to see if the scentair field is also entered for this position, if not, move focus to that
                // if there is a scentair code already entered, move to next active bay mitec field.
                testRun.bayItems[position].mitecBarcode=candidateText;
                nextBay = testRun.setNextBarcodeEditField();
                lastMitecBarcode=candidateText;
            } else {
                // Dupes are not allowed, keep focus here
                testRun.bayItems[position].isEditMitec=true;
            }
        } else if (candidateText.endsWith(scentairCheckString)) {
            if (!candidateText.equals(lastScentairBarcode)) {
                // This is a valid scentair barcode.  Put it where it belongs and keep focus here.
                testRun.bayItems[position].scentairBarcode=candidateText;
                nextBay = testRun.setNextBarcodeEditField();
                lastScentairBarcode=candidateText;
            } else {
                // Dupes are not allowed, keep the focus here.
                testRun.bayItems[position].isEditMitec=true;
            }
        } else {
            // This is not a valid barcode for either type.  Keep focus here
            testRun.bayItems[position].isEditMitec=true;
        }
        updateCounts();
        if (!nextBay.equals(-1)) {
            listView.setSelection(nextBay);
        }
        aa.notifyDataSetChanged();
    }

    private void completeTestStep () {
        // This is the only way to finish this step and move to the next step
        // This button is only active if all active bays have been passed or failed.
        Boolean moveToNextStep = true;
        Boolean showView = false;

        if ((testRun.overallUnitsFailed + testRun.currentStepUnitsFailed)>=testRun.numberOfActiveBays) {
            // This is a special case where all units have failed
            // End the test run here, there is no more to say
            // do not load the next step
            //Update NVM to show the last test run was completed
            editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE,false);
            editor.putString(TAG_SAVED_TEST_RUN, "");
            editor.commit();
            moveToNextStep=false;
            postTestResults();
            finish();
        } else if ( testRun.currentStepUnitsTested >= testRun.numberOfActiveBays) {
            // There should be at least one unit that is still eligible to pass the run
            // Turn on the Complete Step button.
            moveToNextStep=true;
        } else {
            for (int i = 0; i < rack.numberOfBays; i++) {
                if (testRun.bayItems[i].isActive) {
                    // Only check active bays
                    if (testRun.bayItems[i].stepStatus.equals("Not Tested")) {
                        moveToNextStep = false;
                    }
                }
            }
        }
        if (moveToNextStep) {
            showView=loadNextStep();
        }
        if (showView) updateView();
    }

    protected Boolean loadNextStep() {
        // This is the code to load the next test step and reset the variables for a new run
        // through the bay list
        // Here we need to reset the background colors
        // move the text to the next step
        // reset the various counters
        // go back to the top of the list
        Boolean returnValue=false;

        for (int i=0;i<rack.numberOfBays;i++) {
            // Only check active bays
            if (testRun.bayItems[i].isActive) {
                //If any unit has failed this step, it will be exempt from future steps.
                if (testRun.bayItems[i].stepStatus.equals("Failed")) {
                    //It has failed, skip it for the rest of the run.
                    testRun.bayItems[i].stepStatus = "Failed previous step";
                    testRun.bayItems[i].isFailed = true;
                    testRun.bayItems[i].failStep = testRun.currentTestStep;
                    testRun.overallUnitsFailed++;
                    testRun.bayItems[i].lcdState = "OFF";
                } else if (testRun.bayItems[i].stepStatus.equals("Passed")) {
                    testRun.bayItems[i].stepStatus = "Not Tested";
                    testRun.bayItems[i].lcdState = "ON";
                }
            } else testRun.bayItems[i].stepStatus = "Inactive";
        }
        //reset counters per step
        testRun.currentStepUnitsFailed = 0;
        testRun.currentStepUnitsPassed = 0;
        testRun.currentStepUnitsTested = 0;
        // Turn off complete step button
        showCompleteStepButton="";
        // Update the step complete timestamp
        testRun.testResult.setEndTime(testRun.currentTestStep-1);
        // go to next step
        testRun.currentTestStep++;
        // Check if that is the end of the steps and end of this run
        if (testRun.currentTestStep>testRun.maxTestSteps)
        {
            // End of this run, report results.
            postTestResults();
            //close this activity
            finish();
        }
        else {
            // There is at least one more step left in this run
            returnValue=true;
            // Turn the bay lights back on
            for (int i=0;i<rack.numberOfBays;i++) {
                if (testRun.bayItems[i].isActive && !testRun.bayItems[i].isFailed) {
                    if (testRun.currentTestStep.equals(5)) {
                        // Special check to see if the cycle test (step 5) has already passed
                        if (!testRun.bayItems[i].cycleTestComplete) {
                            testRun.bayItems[i].lcdState="ON";
                            updateLED(i, true);
                        } else {
                            // The cycle test has already passed, so mark this bay passed
                            testRun.bayItems[i].stepStatus="Passed";
                            // Turn off readings from that bay

                        }
                    } else {
                        testRun.bayItems[i].lcdState = "ON";
                        updateLED(i, true);
                    }
                }
            }
            //Update the step begun timestamp
            testRun.testResult.setStartTime(testRun.currentTestStep - 1);
            //scroll back to the top of the list
            listView.smoothScrollToPosition(0);
        }
        return returnValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            for (int i = 0; i < rack.numberOfPhidgetsPerRack; i++) {
                rack.phidgets[i].phidget.close();
            }
        }
        catch (PhidgetException pe) {
            pe.printStackTrace();
        }
        rack = null;
    }

    private void updateCounts(){
        testRun.currentStepUnitsFailed=0;
        testRun.currentStepUnitsPassed=0;
        testRun.overallUnitsFailed=0;
        testRun.currentStepUnitsTested=0;
        showCompleteStepButton="";
        // update results totals
        for (int i=0;i<rack.numberOfBays;i++) {
            // Only need to consider active bays
            if (rack.bays[i].active) {
                String returnValue = testRun.bayItems[i].isPassReady(testRun.currentTestStep);
                if (returnValue.equals("Passed")) {
                    testRun.bayItems[i].stepStatus = "Passed";
                    testRun.bayItems[i].lcdState = "OFF";
                }
                //count up the numbers of passed and failed units
                if (testRun.bayItems[i].stepStatus.equals("Failed")) {
                    testRun.currentStepUnitsFailed = testRun.currentStepUnitsFailed + 1;
                    testRun.bayItems[i].lcdState = "OFF";
                }
                if (testRun.bayItems[i].stepStatus.equals("Passed")) {
                    testRun.currentStepUnitsPassed = testRun.currentStepUnitsPassed + 1;
                    testRun.bayItems[i].lcdState = "OFF";
                }
                if (testRun.bayItems[i].stepStatus.equals("Failed previous step")) {
                    testRun.overallUnitsFailed = testRun.overallUnitsFailed + 1;
                    testRun.bayItems[i].lcdState = "OFF";
                }
                Boolean turnOn = false;
                if (testRun.bayItems[i].lcdState.equals("ON")) turnOn=true;
                updateLED(i, turnOn);
            }
        }
        testRun.currentStepUnitsTested = testRun.currentStepUnitsFailed+testRun.currentStepUnitsPassed+testRun.overallUnitsFailed;
        if ((testRun.overallUnitsFailed>=testRun.numberOfActiveBays) ||
                testRun.currentStepUnitsFailed>=testRun.numberOfActiveBays) {
            // This is a special case where all units have failed
            // End the test run here, there is no more to say
            // do not load the next step
            showCompleteStepButton="FAIL";
            listView.setSelection(aa.getCount()-1);
        } else if ( testRun.currentStepUnitsTested >= testRun.numberOfActiveBays) {
            // There should be at least one unit that is still eligible to pass the run
            // Turn on the Complete Step button.
            showCompleteStepButton="SHOW";
            listView.setSelection(aa.getCount()-1);
        }
        // Only show the view and update saved state if we are not at the end of the run
        updateView();
        saveTestRunState();
    }

    private void updateView(){
        if (testRun!=null) {
            TestStep testStep = testSteps.get(testRun.currentTestStep - 1);

            //Get the header info loaded from the data structure
            TextView currentStep = (TextView) findViewById(R.id.teststepnumber);
            String text = "Step " + testRun.currentTestStep.toString() + " of " + testRun.maxTestSteps.toString();
            currentStep.setText(text);
            //Load the current step start time
            TextView currentStepStartTime = (TextView) findViewById(R.id.start_time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd hh:mm:ss", Locale.US);
            String dateToStr = format.format(testRun.testResult.getStartTime(testRun.currentTestStep - 1));
            currentStepStartTime.setText(dateToStr);
            //Get the current progress info loaded
            TextView currentProgress = (TextView) findViewById(R.id.test_step_progress);
            Integer baysRemaining = testRun.numberOfActiveBays - testRun.overallUnitsFailed;
            text = "Tested " + testRun.currentStepUnitsTested.toString() + "/" + baysRemaining.toString();
            currentProgress.setText(text);
            TextView activeBays = (TextView) findViewById(R.id.active_bays);
            text = testRun.numberOfActiveBays.toString();
            activeBays.setText(text);
            TextView skippedBaysView = (TextView) findViewById(R.id.skipped_bays);
            Integer skippedBays = testRun.overallUnitsFailed;
            text = skippedBays.toString();
            skippedBaysView.setText(text);
            TextView passedView = (TextView) findViewById(R.id.number_passed);
            text = testRun.currentStepUnitsPassed.toString();
            passedView.setText(text);
            TextView failedView = (TextView) findViewById(R.id.number_failed);
            text = testRun.currentStepUnitsFailed.toString();
            failedView.setText(text);
            if (highlightFailed) {
                failedView.setBackgroundColor(Color.YELLOW);
                highlightFailed = false;
            } else {
                failedView.setBackgroundColor(Color.WHITE);
            }
            //Get the verify list loaded
            TextView verifyText = (TextView) findViewById(R.id.teststepverifylist);
            text = testStep.expectedResults;
            String newLine = System.getProperty("line.separator");
            String newText = text.replaceAll("NEWLINE", newLine);
            verifyText.setSingleLine(false);
            verifyText.setText(newText);
            //Get the test step information from the Test Steps list
            TextView stepInfo = (TextView) findViewById(R.id.teststepinstruction);
            text = testStep.testSteps;
            newText = text.replaceAll("NEWLINE", newLine);
            stepInfo.setSingleLine(false);
            stepInfo.setText(newText);
            if (showCompleteStepButton.equals("SHOW")) {
                // This step is finished
                // Activate the button and set the color and text
                completeStepButton.setBackgroundColor(Color.GREEN);
                completeStepButton.setTextColor(Color.BLACK);
                completeStepButton.setText("Step Complete.  Next Step->");
            } else if (showCompleteStepButton.equals("FAIL")) {
                // This is a failed run.  Indicate jump to post results
                completeStepButton.setBackgroundColor(Color.RED);
                completeStepButton.setTextColor(Color.BLACK);
                completeStepButton.setText("All units failed.  Post Results->");

            } else {
                completeStepButton.setBackgroundColor(Color.GRAY);
                completeStepButton.setTextColor(Color.BLACK);
                Integer baysPending = testRun.numberOfActiveBays - testRun.currentStepUnitsTested - testRun.overallUnitsFailed;
                String buttonText = "Bays Still Require Testing : " + baysPending;
                completeStepButton.setText(buttonText);
            }
            aa.notifyDataSetChanged();
        }
    }

    private void saveTestRunState () {
        // get test run state info translated to a string
        testRun.currentBay=testRun.currentStepUnitsTested;
        testRunSavedState = gson.toJson(testRun);
        //update NVM to save state and start on next step on restart/reboot
        editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE,true);
        editor.putString(TAG_SAVED_TEST_RUN,testRunSavedState);
        editor.commit();
    }

    private void postTestResults() {
        //Calculate results
        testRun.calculateResults(rack);
        // We serialize the test run object to save it here via JSON
        // The run includes the results we just calculated
        testRunSavedState = gson.toJson(testRun);
        // Load up the intent with the data and crank up the new activity
        Intent newIntent = new Intent(context,PostTestResultActivity.class);
        newIntent.putExtra("TestRun",testRunSavedState);
        newIntent.putExtra("RackBays",rack.numberOfBays);
        startActivity(newIntent);
        finish();
    }

    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
            Integer sensorChangeTrigger = resources.getInteger(R.integer.PHIDGET_SENSOR_CHANGE_TRIGGER);
            Integer dataRate = resources.getInteger(R.integer.PHIDGET_DATA_RATE);
            Boolean ratioMetric = resources.getBoolean(R.bool.PHIDGET_RATIO_METRIC);
            try {
                if (phidget.isAttached()) {
                    if (phidget==rack.phidgets[0].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[0].phidget.setDataRate(i, dataRate);
                            rack.phidgets[0].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[0].phidget.setRatiometric(false);
                    } else if (phidget==rack.phidgets[1].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[1].phidget.setDataRate(i, dataRate);
                            rack.phidgets[1].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[1].phidget.setRatiometric(false);
                    } else if (phidget==rack.phidgets[2].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[2].phidget.setDataRate(i, dataRate);
                            rack.phidgets[2].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[2].phidget.setRatiometric(ratioMetric);
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
                updateView();
            }
        }
    }

    class SensorChangeRunnable implements Runnable {
        int phidgetNumber,sensorIndex, sensorVal;

        public SensorChangeRunnable(int phidgetNumber, int index, int val) {
            this.sensorIndex = index;
            this.sensorVal = val;
            this.phidgetNumber=phidgetNumber;
        }
        public void run() {
            // Put the value from the phidget into the
            Integer bayValue = (phidgetNumber*8)+sensorIndex;
            Integer updatedValue=0;
            if (rack.bays!=null) {
                updatedValue = sensorVal + rack.bays[bayValue].calibrationOffset;
            }
            Boolean refreshScreen = testRun.bayItems[bayValue].updateValue(updatedValue,testRun.currentTestStep);
            aa.notifyDataSetChanged();
            if (refreshScreen) {
                updateCounts();
            }
        }
    }

    @Override
    public void onMitecBarCodeFocusChangeListener(int position, boolean touchFocusSelect) {
        // Operator has touched a field, shift the edit and focus to that field and clear the others.
        //clear the old edit field because the user has selected this field.
        if (touchFocusSelect) {
            for (int i = 0; i < testRun.bayItems.length; i++) {
                testRun.bayItems[i].isEditScentair = false;
                testRun.bayItems[i].isEditMitec = false;
            }
            // Clear the field for re-entry
            testRun.bayItems[position].mitecBarcode = "";
            testRun.bayItems[position].isEditMitec = true;
            testRun.bayItems[position].stepStatus="Not Tested";
            aa.notifyDataSetChanged();
        }
    }

    @Override
    public void onScentAirBarCodeFocusChangeListener(int position, boolean touchFocusSelect) {
        //clear the old edit field because the user has selected this field.
        if (touchFocusSelect) {
            for (int i = 0; i < testRun.bayItems.length; i++) {
                testRun.bayItems[i].isEditScentair = false;
                testRun.bayItems[i].isEditMitec = false;
            }
            // Check to see if something else was entered.  If so, clear it
            testRun.bayItems[position].scentairBarcode = "";
            testRun.bayItems[position].isEditScentair = true;
            testRun.bayItems[position].stepStatus="Not Tested";
            aa.notifyDataSetChanged();
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
            testRun = new TestRun();
            if (resume) {
                // Get the saved test run from prefs to start on from preferences
                testRunSavedState = sharedPreferences.getString(TAG_SAVED_TEST_RUN,"");
                testRun = gson.fromJson(testRunSavedState,TestRun.class);
                Toast.makeText(getApplicationContext(), "Test Run Resumed", Toast.LENGTH_LONG).show();
            }
            else {
                //Initialize this test run
                testRun = new TestRun(currentOperator, rack,testSteps.size());
                // Make sure we read the test steps from the proper data structure
                // Reset resume status and clear saved test run
                editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE, false);
                editor.putString(TAG_SAVED_TEST_RUN,"");
                editor.commit();
                // DEBUG - check if barcode debug is set.  If so, through some junk into the barcodes
                // so we can move on to the rest of the tests
                Boolean barcodeOverride = sharedPreferences.getBoolean(MainActivity.DEBUG_BARCODE_OVERRIDE, false);
                if (barcodeOverride) {
                    // Mitec barcodes are set to letters, scentair to numbers
                    testRun.bayItems[0].mitecBarcode="AAA123";
                    testRun.bayItems[0].scentairBarcode="123AAA";
                    testRun.bayItems[1].mitecBarcode="AAA123333";
                    testRun.bayItems[1].scentairBarcode="124AAA";
                    testRun.bayItems[2].mitecBarcode="AAA128";
                    testRun.bayItems[2].scentairBarcode="127AAA";
                    testRun.bayItems[3].mitecBarcode="AAA123111";
                    testRun.bayItems[3].scentairBarcode="122111AAA";
                    testRun.bayItems[4].mitecBarcode="AAA124";
                    testRun.bayItems[4].scentairBarcode="123AAA";
                    testRun.bayItems[5].mitecBarcode="AAA122";
                    testRun.bayItems[5].scentairBarcode="121AAA";
                    testRun.bayItems[6].mitecBarcode="AAA121";
                    testRun.bayItems[6].scentairBarcode="121AAA";
                    testRun.bayItems[7].mitecBarcode="AAA128";
                    testRun.bayItems[7].scentairBarcode="1212AAA";
                    testRun.bayItems[8].mitecBarcode="AAA1123";
                    testRun.bayItems[8].scentairBarcode="12352AAA";
                    testRun.bayItems[9].mitecBarcode="AAA1252";
                    testRun.bayItems[9].scentairBarcode="1231245AAA";
                    testRun.bayItems[10].mitecBarcode="AAA1256123";
                    testRun.bayItems[10].scentairBarcode="12312AAA";
                    testRun.bayItems[11].mitecBarcode="AA12A123";
                    testRun.bayItems[11].scentairBarcode="12351A2AA";
                    testRun.bayItems[12].mitecBarcode="AA12A11223";
                    testRun.bayItems[12].scentairBarcode="123A12A1A";
                    testRun.bayItems[13].mitecBarcode="AA11A12123";
                    testRun.bayItems[13].scentairBarcode="1213A1A23A";
                    testRun.bayItems[14].mitecBarcode="AA215A1123";
                    testRun.bayItems[14].scentairBarcode="123AA215A";
                    testRun.bayItems[15].mitecBarcode="AAA122213";
                    testRun.bayItems[15].scentairBarcode="1232A1A2A";
                    testRun.bayItems[16].mitecBarcode="AA2A12253";
                    testRun.bayItems[16].scentairBarcode="12232A1A1A";
                    testRun.bayItems[17].mitecBarcode="A6AA123";
                    testRun.bayItems[17].scentairBarcode="12993AAA";
                    testRun.bayItems[18].mitecBarcode="A4AA123";
                    testRun.bayItems[18].scentairBarcode="1263453A4AA";
                    testRun.bayItems[19].mitecBarcode="A4AA41523";
                    testRun.bayItems[19].scentairBarcode="123A7A43A";
                    testRun.bayItems[20].mitecBarcode="A333AA123";
                    testRun.bayItems[20].scentairBarcode="12333AA3A";
                    testRun.bayItems[21].mitecBarcode="A888AA3123";
                    testRun.bayItems[21].scentairBarcode="132436AA33A";
                    testRun.bayItems[22].mitecBarcode="A2345AA123";
                    testRun.bayItems[22].scentairBarcode="123AA2344A";
                    testRun.bayItems[23].mitecBarcode="A3A33A2123";
                    testRun.bayItems[23].scentairBarcode="1223453A5A5A";
                }
            }
            aa= new BayItemArrayAdapter(context, testRun);
            aa.setCustomButtonListener(TestRunActivity.this);
            listView.setAdapter(aa);
            if (resume) listView.smoothScrollToPosition(testRun.currentBay);
            if (testRun.currentTestStep.equals(1)) {
                // Make sure to put the cursor on the correct field
                // Check the first bay
                Integer targetBay = testRun.setNextBarcodeEditField();
                listView.setSelection(targetBay);
            }
            // add the phidget interface stuff for the real time value.
            try {
                for (int i=0;i<rack.numberOfPhidgetsPerRack;i++) {
                    // This should lower the data rate to the minimum value
                    // and lower the threshold to provide new change events
                    //for (int j=0;j<7;j++) {
                    //    rack.phidgets[i].phidget.setDataRate(j,100);
                    //    rack.phidgets[i].phidget.setSensorChangeTrigger(j,15);
                    //}
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
                    final int finalI = i;
                    rack.phidgets[i].phidget.addSensorChangeListener(new SensorChangeListener() {
                        public void sensorChanged(SensorChangeEvent se) {
                            runOnUiThread(new SensorChangeRunnable(finalI, se.getIndex(), se.getValue()));
                        }
                    });
/*  Until we need it
            ik.addInputChangeListener(new InputChangeListener() {
                public void inputChanged(InputChangeEvent ie) {
                    runOnUiThread(new InputChangeRunnable(ie.getIndex(), ie.getState()));
                }
            });
*/
                    //ik.openAny(phidgetServerAddress, 5001);
                    rack.phidgets[i].phidget.open(rack.phidgets[i].phidgetSerialNumber, phidgetServerAddress, 5001);
                }
            } catch (PhidgetException pe) {
                pe.printStackTrace();
            }
            updateCounts();
            updateView();
        }
    }
//    class InputChangeRunnable implements Runnable {
//        int index;
//        boolean val;
//        public InputChangeRunnable(int index, boolean val)
//        {
//            this.index = index;
//            this.val = val;
//        }
//        public void run() {
//            if(inputCheckBoxes[index]!=null)
//                inputCheckBoxes[index].setChecked(val);
//        }
//    }
}
