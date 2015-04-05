package com.scentair.scentwave;

public class BayItem{
    public Bay bay;

    public String mitecBarcode;
    public String scentairBarcode;
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
                   Integer currentValue){
        this.bay = new Bay(bayNumber);
        this.mitecBarcode = mitecBarcode;
        this.scentairBarcode = scentairBarcode;
        this.unitState = unitState;
        this.currentValue = currentValue;
        this.stepStatus = "Not Tested";
        this.failCause = "";
        this.failStep = 0;
        this.failCauseIndex=0;
    }
}