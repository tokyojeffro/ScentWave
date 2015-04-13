package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.scentair.scentwave.BayItemArrayAdapter.customButtonListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class TestRunActivity extends Activity implements customButtonListener {

    public static final String TAG_SAVED_TEST_RUN="SAVED_TEST_RUN";

    private InterfaceKitPhidget ik;
    private String phidgetServerAddress;
    private MachineStates machineStates;

    TestRun testRun;
    Rack rack;
    ArrayList<TestStep> testSteps = MainActivity.testSteps.getTestSteps();
    ListView listView;
    BayItemArrayAdapter aa;
    Context context;
    ArrayList<Failure> failureList;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public Integer currentRack;
    public String currentOperator;
    private boolean highlightFailed = false;
    private View footerView;

    // These are used to save the current state of the test run to prefs
    private String testRunSavedState;
    Gson gson;

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
            resume=extras.getBoolean(MainActivity.TAG_RESUME_AVAILABLE);
        }

        // Need to make sure we pull out the calibration info before starting the test run
        // Pull the associated rack number from NVM
        currentRack = sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER,1);
        currentOperator = sharedPreferences.getString(MainActivity.TAG_OPERATOR_NAME,"");
        //String dbAddress = sharedPreferences.getString(MainActivity.TAG_DATABASE_SERVER_ADDRESS,"");
        phidgetServerAddress = sharedPreferences.getString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS,"192.168.1.22");

        failureList = MainActivity.failures.getFailures();
        rack = MainActivity.rack.getRack();

        //Finally, set up the machine states mapping
        machineStates = new MachineStates();

        //Initialize Gson object;
        gson = new Gson();

        testRun = new TestRun();

        if (resume) {
            // Get the saved test run from prefs to start on from preferences
            testRunSavedState = sharedPreferences.getString(TAG_SAVED_TEST_RUN,"");

            testRun = gson.fromJson(testRunSavedState,TestRun.class);
        }
        else {
            //Initialize this test run
            testRun = new TestRun(currentOperator, MainActivity.rack,testSteps.size());
            // Make sure we read the test steps from the proper data structure


            // Reset resume status and clear saved test run
            editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE, false);
            editor.putString(TAG_SAVED_TEST_RUN,"");
            editor.commit();
        }

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.list_view);
        listView.setItemsCanFocus(true);

        footerView =  ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.baylistfooter, null, false);
        listView.addFooterView(footerView);

        Button passAllButton = (Button) findViewById(R.id.pass_all_button);
        passAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passAll();
            }
        });

        aa= new BayItemArrayAdapter(this, testRun);
        aa.setCustomButtonListener(TestRunActivity.this);

        listView.setAdapter(aa);

        if (resume) listView.smoothScrollToPosition(testRun.currentBay);

        // add the phidget interface stuff for the real time value.
        try
        {
            ik = new InterfaceKitPhidget();
            ik.addAttachListener(new AttachListener() {
                public void attached(final AttachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), true);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addDetachListener(new DetachListener() {
                public void detached(final DetachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), false);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addSensorChangeListener(new SensorChangeListener() {
                public void sensorChanged(SensorChangeEvent se) {
                    runOnUiThread(new SensorChangeRunnable(se.getIndex(), se.getValue()));
                }
            });
/*  Until we need it
            ik.addInputChangeListener(new InputChangeListener() {
                public void inputChanged(InputChangeEvent ie) {
                    runOnUiThread(new InputChangeRunnable(ie.getIndex(), ie.getState()));
                }
            });
*/
            ik.openAny(phidgetServerAddress, 5001);
        }
        catch (PhidgetException pe)
        {
            pe.printStackTrace();
        }
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

        //Check to see if they are marking a previous failed unit as passed.
        //if so, clear the data and reset to untested.
        if (testRun.bayItems[position].stepStatus.equals("Failed")) {
            // Clear the previous failure cause
            testRun.bayItems[position].failCause = "";
            // Reset tested status
            testRun.bayItems[position].stepStatus = "Not Tested";
        } else {
            testRun.bayItems[position].stepStatus = "Passed";
            listView.smoothScrollToPosition(position+2);
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

        // Here we need an AlertDialog that provides a list of potential failure reasons
        TestStep testStep = testSteps.get(testRun.currentTestStep-1);

        final CharSequence[] failureStrings = new CharSequence[testStep.possibleFailures.size()];

        for (int i=0;i<testStep.possibleFailures.size();i++) {
            Integer failureOffset = testStep.possibleFailures.get(i)-1;
            Failure failure = failureList.get(failureOffset);
            failureStrings[i] = failure.failureText;
        }

        testRun.bayItems[position].stepStatus = "Failed";
        testRun.bayItems[position].failStep = testRun.currentTestStep;


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
                        testRun.bayItems[bayPosition].failCause = failureStrings[which].toString();
                        aa.notifyDataSetChanged();
                    }
                })
                .setView(dialogView)
                .show();

        testRun.bayItems[bayPosition].failStep=testRun.currentTestStep;

        // If we are in test step 1 (entering barcodes, then move the cursor to the next active bay)
        if (testRun.currentTestStep==1) {
            Integer nextBay = testRun.getNextActiveBay(position);
            // Clear any edits from this bay
            testRun.bayItems[position].isEditMitec=false;
            testRun.bayItems[position].isEditScentair=false;
            // Set the focus to the next active bay
            testRun.bayItems[position].isEditMitec=true;
        }
        listView.smoothScrollToPosition(position+2);
        updateCounts();
    }

    @Override
    public void onScentAirBarCodeClickListener(int position, String candidateText) {
        // Scentair barcode has been entered, need to scroll to the next row
        // skip bays that are inactive
        Integer nextBay = 0;
        testRun.bayItems[position].isEditScentair=false;

        // First, check to see if the barcode is a valid scentair barcode
        if (candidateText.endsWith(".00")) {
            // This is a valid scentair barcode
            // Save it into the array
            // Check to see if the mitec field is also entered for this position, then move focus to the next row mitec field
            // If the mitec field is not entered, keep the focus on this row.
            testRun.bayItems[position].scentairBarcode=candidateText;

            if (testRun.bayItems[position].mitecBarcode.isEmpty()) {
                // Keep the focus on this row before moving on.
                nextBay = position;

            } else nextBay = testRun.getNextActiveBay(position);

            testRun.bayItems[nextBay].isEditMitec = true;

            if (nextBay<rack.numberOfBays) {
                // if there are bays left to scroll to, move on down
                listView.setSelection(nextBay);
            }
        } else if (candidateText.contains("REV")) {
            // This is a valid Mitec barcode.  Put it where it belongs and keep focus here.
            testRun.bayItems[position].mitecBarcode=candidateText;
            testRun.bayItems[position].isEditScentair=true;
        } else {
            // This is not a valid barcode for either type.  Keep focus here
            testRun.bayItems[position].isEditScentair=true;
        }
        aa.notifyDataSetChanged();
    }

    @Override
    public void onMitecBarCodeClickListener(int position, String candidateText) {
        // Something has been entered into the mitec field
        // validate it and enter it if is good.  then move to scentair field
        // need to move focus and cursor to scentair barcode

        Integer nextBay = 0;
        testRun.bayItems[position].isEditMitec=false;

        // First, check to see if the barcode is a valid scentair barcode
        if (candidateText.contains("REV")) {
            // This is a valid mitec barcode
            // Save it into the array
            // Check to see if the scentair field is also entered for this position, if not, move focus to that
            // if there is a scentair code already entered, move to next active bay mitec field.
            testRun.bayItems[position].mitecBarcode=candidateText;

            if (testRun.bayItems[position].scentairBarcode.isEmpty()) {
                // Keep the focus on this row before moving on.
                nextBay = position;
                testRun.bayItems[nextBay].isEditScentair=true;
            } else {
                nextBay = testRun.getNextActiveBay(position);
                if (nextBay<rack.numberOfBays) {
                    // if there are bays left to scroll to, move on down
                    testRun.bayItems[nextBay].isEditMitec = true;
                    listView.setSelection(nextBay);
                }
            }
        } else if (candidateText.endsWith(".00")) {
            // This is a valid scentair barcode.  Put it where it belongs and keep focus here.
            testRun.bayItems[position].scentairBarcode=candidateText;
            testRun.bayItems[position].isEditMitec=true;
        } else {
            // This is not a valid barcode for either type.  Keep focus here
            testRun.bayItems[position].isEditMitec=true;
        }
        aa.notifyDataSetChanged();
    }


    private void passAll () {
        // The operator has scrolled to the end of the bay list and pressed pass all.
        // Update the data to be all 'pass' (do not flag any fails here)

        // This button should only work if there were no failed tests so far
        if (testRun.currentStepUnitsFailed == 0) {
            for (int i = 0; i < rack.numberOfBays; i++) {
                //reset background colors
                if (testRun.bayItems[i].stepStatus.equals("Not Tested")) {
                    testRun.bayItems[i].stepStatus = "Passed";
                }
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

        for (int i=0;i<rack.numberOfBays;i++) {
            //If any unit has failed this step, it will be exempt from future steps.
            if (testRun.bayItems[i].stepStatus.equals("Failed")) {
                //It has failed, skip it for the rest of the run.
                testRun.bayItems[i].stepStatus="Failed previous step";
                testRun.bayItems[i].isFailed=true;
                testRun.bayItems[i].failStep=testRun.currentTestStep;
                testRun.overallUnitsFailed++;
            } else if (testRun.bayItems[i].stepStatus.equals("Passed")) {
                testRun.bayItems[i].stepStatus = "Not Tested";
            }
        }

        testRun.currentStepUnitsFailed = 0;
        testRun.currentStepUnitsPassed = 0;
        testRun.currentStepUnitsTested = 0;
        // Update the step complete timestamp
        testRun.testResult.setEndTime(testRun.currentTestStep-1);

        testRun.currentTestStep++;

        // Check if that is the end of the steps and end of this run
        if (testRun.currentTestStep>testRun.maxTestSteps)
        {
            // End of this run, report results.

            //Update NVM to show the last test run was completed
            editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE,false);
            editor.putString(TAG_SAVED_TEST_RUN, "");
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

            //Update the step begun timestamp
            testRun.testResult.setStartTime(testRun.currentTestStep-1);

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
        for (int i=0;i<rack.numberOfBays;i++) {
            //count up the numbers of passed and failed units
            if (testRun.bayItems[i].stepStatus.equals("Failed")) {
                testRun.currentStepUnitsFailed=testRun.currentStepUnitsFailed+1;
            }
            if (testRun.bayItems[i].stepStatus.equals("Passed")) {
                testRun.currentStepUnitsPassed=testRun.currentStepUnitsPassed+1;
            }
            if (testRun.bayItems[i].stepStatus.equals("Failed previous step")) {
                testRun.overallUnitsFailed=testRun.overallUnitsFailed+1;
            }
        }

        testRun.currentStepUnitsTested = testRun.currentStepUnitsFailed+testRun.currentStepUnitsPassed;

        Boolean showView=true;

        if ( testRun.currentStepUnitsTested >= (testRun.numberOfActiveBays - testRun.overallUnitsFailed) ) {
            showView=loadNextStep();
        }
        // Only show the view and update saved state if we are not at the end of the run
        if (showView) {
            updateView();
            saveTestRunState();
        }
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
        String dateToStr = format.format(testRun.testResult.getStartTime(testRun.currentTestStep-1));
        currentStepStartTime.setText(dateToStr);

        //Get the current progress info loaded
        TextView currentProgress = (TextView) findViewById(R.id.test_step_progress);
        Integer baysRemaining = testRun.numberOfActiveBays - testRun.overallUnitsFailed;
        text="Tested " + testRun.currentStepUnitsTested.toString() + "/" + baysRemaining.toString();
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
        String newLine = System.getProperty("line.separator");
        String newText=text.replaceAll("NEWLINE",newLine);
        verifyText.setSingleLine(false);
        verifyText.setText(newText);

        //Get the test step information from the Test Steps list
        TextView stepInfo = (TextView) findViewById(R.id.teststepinstruction);
        text = testStep.testSteps;
        newText=text.replaceAll("NEWLINE",newLine);
        stepInfo.setSingleLine(false);
        stepInfo.setText(newText);

        //If any of the barcode fields are being edited, show the soft keyboard
        Boolean showKeyboard=false;
        for (int i = 0; i < testRun.bayItems.length; i++) {
            if (testRun.bayItems[i].isEditScentair || testRun.bayItems[i].isEditMitec) {
                showKeyboard = true;
            }
        }
        if (showKeyboard) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //TODO Gonna need to fix this mess
        }

        // If the test step does not need a 'Pass All' button at the bottom, disable it here
        if (testRun.currentTestStep==1) {
            // Turn off the footer view
            footerView.setVisibility(View.INVISIBLE);
        } else {
            footerView.setVisibility(View.VISIBLE);
        }

        aa.notifyDataSetChanged();
    }

    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
        }
        public void run() {
/*            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);
            if(attach)
            {
                attachedTxt.setText("Attached");
                try {
                    TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
                    TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
                    TextView versionTxt = (TextView) findViewById(R.id.versionTxt);

                    nameTxt.setText(phidget.getDeviceName());
                    serialTxt.setText(Integer.toString(phidget.getSerialNumber()));
                    versionTxt.setText(Integer.toString(phidget.getDeviceVersion()));

                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
            else
                attachedTxt.setText("Detached");*/
            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class SensorChangeRunnable implements Runnable {
        int sensorIndex, sensorVal;
        public SensorChangeRunnable(int index, int val)
        {
            this.sensorIndex = index;
            this.sensorVal = val;
            //TODO need to add in the offset for the different phidgets for this rack

            Log.i("TestRunSensorReadouts", String.valueOf(sensorIndex) + " " + String.valueOf(sensorVal));
        }
        public void run() {
            testRun.bayItems[sensorIndex].currentValue = sensorVal;
            testRun.bayItems[sensorIndex].unitState = machineStates.getState(sensorVal);
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

            aa.notifyDataSetChanged();
            // Clear the field for re-entry
            testRun.bayItems[position].mitecBarcode = "";
            testRun.bayItems[position].isEditMitec = true;
            aa.notifyDataSetChanged();
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
