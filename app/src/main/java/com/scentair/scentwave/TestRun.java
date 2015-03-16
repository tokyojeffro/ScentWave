package com.scentair.scentwave;

import java.security.Timestamp;
import java.util.*;

public class TestRun{
    public Integer rackNumber;
    public Timestamp timeRun;

    public Integer unitsTested;
    public Integer unitsPassed;
    public Integer unitsFailed;

    public UnitTest[] testResults;

    public TestRun(Integer rackNum){
        this.rackNumber = rackNum;
        unitsTested = 24;
        unitsPassed = 0;
        unitsFailed = 0;

        testResults = new UnitTest[24];
    }

}