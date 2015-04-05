package com.scentair.scentwave;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestRun{
    //TODO tie in the correct racknumber through calibration

    // Need to load the phidget info from here
    public Rack rack;
    public SimpleDateFormat timeRun=new SimpleDateFormat();

    public Integer overallUnitsTested=0;
    public Integer overallUnitsPassed=0;
    public Integer overallUnitsFailed=0;

    public Integer currentStepUnitsTested=0;
    public Integer currentStepUnitsPassed=0;
    public Integer currentStepUnitsFailed=0;

    public UnitTest[] testResults;

    public Integer currentTestStep=1;
    public Integer maxTestSteps;
    public Integer numberOfBays=24;
    public Integer currentEditPosition;

    public TestRun(Integer rackNum){
        this.rack = new Rack(rackNum);
        overallUnitsTested = 0;
        overallUnitsPassed = 0;
        overallUnitsFailed = 0;

        testResults = new UnitTest[numberOfBays];
    }

    public void setCurrentTestStep (int currentStep) {
        currentTestStep=currentStep;
    }
}