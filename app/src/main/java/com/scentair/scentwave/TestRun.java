package com.scentair.scentwave;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestRun{
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
    public Integer numberOfBays;
    public Integer numberOfActiveBays;

    public TestRun(Integer rackNum, String dbAddress){
        this.rack = MainActivity.rack.getRack();
        overallUnitsTested = 0;
        overallUnitsPassed = 0;
        overallUnitsFailed = 0;
        numberOfBays = 24;

        numberOfActiveBays=rack.getActiveBays();

        // Need to keep all bays included here, even though some may be inactive
        testResults = new UnitTest[24];
    }
    public void setCurrentTestStep (int currentStep) {
        currentTestStep=currentStep;
    }
}