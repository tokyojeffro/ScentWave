package com.scentair.scentwave;

import com.google.gson.Gson;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestRun{
    public Integer overallUnitsFailed=0;

    public Integer currentStepUnitsTested=0;
    public Integer currentStepUnitsPassed=0;
    public Integer currentStepUnitsFailed=0;

    //public UnitTest[] testResults;

    public Integer currentTestStep=1;
    public Integer currentBay=0;
    public Integer maxTestSteps;
    public Integer numberOfActiveBays;
    public BayItem[] bayItems;
    public TestResult testResult;

    // Constructor to generate a new test run
    public TestRun(String operator, Rack rack, Integer numTestSteps){
        this.maxTestSteps=numTestSteps;
        overallUnitsFailed = 0;

        this.numberOfActiveBays=rack.getActiveBays();

        this.testResult = new TestResult(operator, rack.number, this.numberOfActiveBays, numTestSteps);

        // Need to keep all bays included here, even though some may be inactive
        bayItems = new BayItem[rack.numberOfBays];

        for(int i=0;i<rack.numberOfBays;i++){
            boolean status = rack.bays[i].active;
            bayItems[i]=new BayItem(i+1, status);
        }

        // The first step is to enter the barcodes.  Set the edit field
        bayItems[0].isEditMitec=true;

        // Set the start time for the first test step
        testResult.setStartTime(0);
    }

    public TestRun () {

    }

    public TestRun getTestRun(){
        return this;
    }

    public Integer getNextActiveBay (Integer position) {
        Integer returnValue=position+1;
        Boolean activeFound=false;

        //Loops through to find the next active bay after the given position
        while (returnValue<bayItems.length && !activeFound) {
            if (bayItems[returnValue].isActive) activeFound=true;
        }

        if (!activeFound) {
            // There were no more active bays left
            // Return 24 to move to bottom of list
            return bayItems.length;
        } else return returnValue;
    }

    public void setCurrentTestStep (int currentStep) {
        currentTestStep=currentStep;
    }
}