package com.scentair.scentwave;


import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Date;

// This is the unit class for a test result for a unit in a run
public class TestResult {
    @Expose public String operator;
    @Expose public Integer rackNumber;
    @Expose public Integer numberOfActiveBays;
    @Expose public Integer numberOfUnitsPassed;
    @Expose public Integer numberOfUnitsFailed;
    @Expose public Date step1Start;
    @Expose public Date step2Start;
    @Expose public Date step3Start;
    @Expose public Date step4Start;
    @Expose public Date step5Start;
    @Expose public Date step1Stop;
    @Expose public Date step2Stop;
    @Expose public Date step3Stop;
    @Expose public Date step4Stop;
    @Expose public Date step5Stop;
    @Expose public String comments;
    // Do not send up the ID, this is what we receive after posting to the database;
    public Integer id;

    // These are not exposed for export because they require special handling
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

        this.unitTests = new ArrayList<>();
        this.comments="";
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
}