package com.scentair.scentwave;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
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

        this.unitTests = new ArrayList<UnitTest>();
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