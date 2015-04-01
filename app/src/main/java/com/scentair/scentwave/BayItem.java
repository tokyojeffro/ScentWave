package com.scentair.scentwave;

public class BayItem{
    public Integer bayNumber;

    public String mitecBarcode;
    public String scentairBarcode;
    public String softwareVersion;
    public String unitState;
    public Integer currentValue;
    public String stepStatus;
    public String failCause;
    public Integer failCauseIndex;
    public Integer failStep;

    public BayItem(Integer bayNumber,
                   String mitecBarcode,
                   String scentairBarcode,
                   String unitState,
                   String softwareVersion,
                   Integer currentValue){
        this.bayNumber = bayNumber;
        this.mitecBarcode = mitecBarcode;
        this.scentairBarcode = scentairBarcode;
        this.unitState = unitState;
        this.currentValue = currentValue;
        this.softwareVersion = softwareVersion;
        this.stepStatus = "Not Tested";
        this.failCause = "";
        this.failStep = 0;
        this.failCauseIndex=0;
    }
}