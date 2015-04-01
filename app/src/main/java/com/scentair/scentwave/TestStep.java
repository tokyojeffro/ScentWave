package com.scentair.scentwave;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestStep {
    public Integer numberOfActions;
    public String testStep1;
    public String testStep2;
    public String expectedResults;
    public ArrayList<Integer> possibleFailures;
    public Date stepStartTime;
    public Date stepEndTime;

    public TestStep (Integer numberOfActions,
                     String testStep1,
                     String testStep2,
                     String expectedResults,
                     ArrayList<Integer> possibleFailures){
        this.numberOfActions=numberOfActions;
        this.testStep1=testStep1;
        this.testStep2=testStep2;
        this.expectedResults=expectedResults;
        this.possibleFailures = possibleFailures;
    }

    // Empty constructor so I can load values later
    public TestStep (){
        possibleFailures = new ArrayList<Integer>();
    };

    public void setStartTime() {
        this.stepStartTime = new Date();
    }

    public void setEndTime() {
        this.stepEndTime = new Date();
    }

    public Date getStartTime () {
        return this.stepStartTime;
    }

    public Date getEndTime () {
        return this.stepEndTime;
    }
}