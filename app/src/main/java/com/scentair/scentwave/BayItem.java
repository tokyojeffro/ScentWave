package com.scentair.scentwave;

public class BayItem{
    public Integer bayNumber;

    public String mitecBarcode;
    public String scentairBarcode;
    public String unitState;
    public Integer currentValue;


    public BayItem(Integer bayNumber,
                   String mitecBarcode,
                   String scentairBarcode,
                   String unitState,
                   Integer currentValue){
        this.bayNumber = bayNumber;
        this.mitecBarcode = mitecBarcode;
        this.scentairBarcode = scentairBarcode;
        this.unitState = unitState;
        this.currentValue = currentValue;
    }

}