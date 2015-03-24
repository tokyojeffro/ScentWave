package com.scentair.scentwave;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.widget.SlidingPaneLayout;
import android.transition.Explode;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.scentair.scentwave.BayItemArrayAdapter.customButtonListener;
import java.lang.Object;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

        bayItems= new BayItem[testRun.numberOfBays];

        for(int i=0;i<testRun.numberOfBays;i++){
            bayItems[i]=new BayItem(i+1,"<empty>","<empty>","Unplugged",0);
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
        aa.notifyDataSetChanged();
    }

    @Override
    public void onFailButtonClickListener(int position, int listViewPosition) {

        int thisPosition = position;

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
