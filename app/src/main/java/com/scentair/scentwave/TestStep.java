package com.scentair.scentwave;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestStep {
    public String testSteps;
    public String expectedResults;
    public ArrayList<Integer> possibleFailures;


    public TestStep (Integer numberOfActions,
                     String testSteps,
                     String expectedResults,
                     ArrayList<Integer> possibleFailures){
        this.testSteps=testSteps;
        this.expectedResults=expectedResults;
        this.possibleFailures = possibleFailures;
    }

    // Empty constructor so I can load values later
    public TestStep (){
        possibleFailures = new ArrayList<Integer>();
    };
}