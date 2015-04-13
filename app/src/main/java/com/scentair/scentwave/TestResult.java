package com.scentair.scentwave;

import java.util.ArrayList;
import java.util.Date;

// This is the unit class for a test result for a unit in a run
public class TestResult {
    public String operator;
    public Integer rackNumber;
    public Integer numberOfActiveBays;
    public Integer numberOfUnitsPassed;
    public Integer numberOfUnitsFailed;
    public Date[] stepStartTimes;
    public Date[] stepEndTimes;
    public ArrayList<UnitTest> unitTests;

    public TestResult(String operator,
                      Integer rackNumber,
                      Integer numberOfActiveBays,
                      Integer numberOfTestSteps) {
        this.operator = operator;
        this.rackNumber=rackNumber;
        this.numberOfActiveBays=numberOfActiveBays;
        this.numberOfUnitsPassed=0;
        this.numberOfUnitsFailed=0;
        stepStartTimes=new Date[numberOfTestSteps];
        stepEndTimes= new Date[numberOfTestSteps];
    }

    public void setStartTime(Integer testStepNumber) {
        this.stepStartTimes[testStepNumber] = new Date();
    }

    public void setEndTime(Integer testStepNumber) {
        this.stepEndTimes[testStepNumber] = new Date();
    }

    public Date getStartTime (Integer testStepNumber) {
        return this.stepStartTimes[testStepNumber];
    }

    public Date getEndTime (Integer testStepNumber) {
        return this.stepEndTimes[testStepNumber];
    }
}