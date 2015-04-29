package com.scentair.scentwave;

import com.google.gson.annotations.Expose;

public class UnitTest {
    // The Unit is not a part of the JSON construction because it must be handled separately
    public SW1004Unit unit;

    // The rest of these are part of the JSON output
    @Expose public Boolean passed;
    @Expose public Integer bayNumber;
    @Expose public String fanMediumDisplayValue;
    @Expose public Integer fanHighValue;
    @Expose public Integer fanMedValue;
    @Expose public Integer fanLowValue;
    @Expose public String failCause;
    @Expose public Integer failType;
    // This is not exposed because we never write the ID out
    public Integer unitTestId;

    public UnitTest(SW1004Unit unit,
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
    }
}