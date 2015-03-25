package com.scentair.scentwave;


import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.scentair.scentwave.BayItemArrayAdapter.customButtonListener;


public class TestRunActivity extends Activity implements customButtonListener {

    TestRun testRun;
    TestSteps testSteps = new TestSteps();
    BayItem[] bayItems;
    ListView listView;
    BayItemArrayAdapter aa;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testrun);

        //Initialize this test run
        //TODO Make sure this input is the rack number from the calibration results
        testRun = new TestRun(1);

        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.list_view);
        listView.setItemsCanFocus(true);

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

        // update results totals
        testRun.currentStepUnitsPassed++;
        testRun.currentStepUnitsTested++;

        View rowView = (View) listView.getChildAt(listViewPosition);

        if ( testRun.currentStepUnitsTested >= testRun.numberOfBays ) {
            // Here we need to reset the background colors
            // move the text to the next step
            // reset the various counters
            // go back to the top of the list
            for (int i=0;i<testRun.numberOfBays;i++) {
                //reset background colors
                bayItems[i].stepStatus = "Not Tested";
            }
            //reset the counters
            testRun.currentStepUnitsPassed = 0;
            testRun.currentStepUnitsTested = 0;
            testRun.currentStepUnitsFailed = 0;

            testRun.currentTestStep++;

            //scroll back to the top of the list
            listView.smoothScrollToPosition(0);

        } else {
            bayItems[position].stepStatus = "Passed";
            listView.smoothScrollToPosition(position+2);
            }
        updateView();
    }

    @Override
    public void onFailButtonClickListener(int position, int listViewPosition) {

        int thisPosition = position;

    }

    @Override
    public void onTextFieldClickListener(int position, int listViewPosition) {

        int thisPosition = position;
        EditText mitecBarcodeText;
        EditText scentairBarcodeText;
        EditText softwareVersionText;
        String scentairBarcodeTemp;
        String mitecBarcodeTemp;
        String softwareVersionTemp;

        View rowView = (View) listView.getChildAt(listViewPosition);
        mitecBarcodeText = (EditText) rowView.findViewById(R.id.mitecbarcode);
        scentairBarcodeText = (EditText) rowView.findViewById(R.id.scentairbarcode);
        softwareVersionText = (EditText) rowView.findViewById(R.id.software_version);
        scentairBarcodeTemp = scentairBarcodeText.getText().toString();
        mitecBarcodeTemp = mitecBarcodeText.getText().toString();
        softwareVersionTemp = softwareVersionText.getText().toString();

        bayItems[position].mitecBarcode = mitecBarcodeTemp;
        bayItems[position].scentairBarcode = scentairBarcodeTemp;
        bayItems[position].softwareVersion = softwareVersionTemp;

        //need to make sure the text stays in the field and the field is redisplayed
        //need to validate the code entered here
        //once validated, need to auto move to the next field

        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateView(){
        //Get the header info loaded from the data structure
        TextView currentStep = (TextView)findViewById(R.id.teststepnumber);
        String text= "Step " + testRun.currentTestStep.toString() + " of " + testRun.maxTestSteps.toString();
        currentStep.setText(text);

        //Get the current progress info loaded
        TextView currentProgress = (TextView) findViewById(R.id.test_step_progess);
        text="Progress " + testRun.currentStepUnitsTested.toString() + "/" + testRun.numberOfBays.toString();
        currentProgress.setText(text);

        //Get the verify list loaded
        TextView verifyText = (TextView) findViewById(R.id.teststepverifylist);
        TestStep testStep = testSteps.getTestStep(testRun.currentTestStep-1);
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
