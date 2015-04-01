package com.scentair.scentwave;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestRun{
    //TODO tie in the correct racknumber through calibration
    public Integer rackNumber=1;
    public SimpleDateFormat timeRun=new SimpleDateFormat();


    public Integer overallUnitsTested=0;
    public Integer overallUnitsPassed=0;
    public Integer overallUnitsFailed=0;

    public Integer currentStepUnitsTested=0;
    public Integer currentStepUnitsPassed=0;
    public Integer currentStepUnitsFailed=0;

    public UnitTest[] testResults;
    public HashMap<Integer, String> failTypes;

    public Integer currentTestStep=1;
    public Integer maxTestSteps;
    public Integer numberOfBays=24;
    public Integer currentEditPosition;

    public TestRun(Integer rackNum){
        this.rackNumber = rackNum;
        overallUnitsTested = 0;
        overallUnitsPassed = 0;
        overallUnitsFailed = 0;

        testResults = new UnitTest[numberOfBays];
        failTypes = new HashMap<Integer, String>();

        this.failTypes.put(1,"Power Failure");
        this.failTypes.put(2,"LCD Failure");
        this.failTypes.put(3,"Power Button Failure");
        this.failTypes.put(4,"Events Settings Button Failure");
        this.failTypes.put(5,"Minus Button Failure");
        this.failTypes.put(6,"Plus Button Failure");
        this.failTypes.put(7,"Clock Button Failure");
        this.failTypes.put(8,"Fan Dial Failure");
        this.failTypes.put(9,"LCD Backlight Failure");
    }

}