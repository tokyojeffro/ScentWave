package com.scentair.scentwave;

public class TestStep {
    public Integer numberOfActions;
    public String testStep1;
    public String testStep2;
    public String expectedResults;

    public TestStep (Integer numberOfActions,
                     String testStep1,
                     String testStep2,
                     String expectedResults){
        this.numberOfActions=numberOfActions;
        this.testStep1=testStep1;
        this.testStep2=testStep2;
        this.expectedResults=expectedResults;
    }
}