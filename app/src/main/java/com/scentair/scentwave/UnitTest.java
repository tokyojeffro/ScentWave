package com.scentair.scentwave;

import java.util.*;

public class UnitTest {
    public Unit unit;
    public Boolean passed;
    public Integer bayNumber;
    public String fanMediumDisplayValue;
    public Integer fanHighValue;
    public Integer fanMedValue;
    public Integer fanLowValue;
    public String failCause;
    public Integer failType;
    public Date timeStamp;

    public UnitTest(Unit unit,
                    Boolean passed,
                    Integer bayNum,
                    String fanMediumDisplayValue,
                    Integer fanHighValue,
                    Integer fanMedValue,
                    Integer fanLowValue,
                    String failCause,
                    Integer failType){
        this.unit = unit;
        this.passed=passed;
        this.bayNumber = bayNum;
        this.fanMediumDisplayValue=fanMediumDisplayValue;
        this.fanHighValue=fanHighValue;
        this.fanMedValue=fanMedValue;
        this.fanLowValue=fanLowValue;
        this.failCause=failCause;
        this.failType=failType;
        this.timeStamp=new Date();
    }
}