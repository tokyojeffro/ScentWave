package com.scentair.scentwave;

import java.util.*;

public class UnitTest {
    public Integer rackNumber;
    public Integer bayNumber;
    public String mitecSerial;
    public String scentairSerial;
    public Map<String, Integer> currentReadings;
    public Map<String, String> testResults;

    public UnitTest(Integer rackNum, Integer bayNum){
        this.rackNumber = rackNum;
        this.bayNumber = bayNum;
        this.mitecSerial = "00000";
        this.scentairSerial = "00000";

        this.currentReadings.put("Unplugged",0);
        this.currentReadings.put("OffPlugged",0);
        this.currentReadings.put("OnPluggedBLOff",0);
        this.currentReadings.put("OnPluggedBLOn",0);
        this.currentReadings.put("FanTurnOn",0);
        this.currentReadings.put("FanLow",0);
        this.currentReadings.put("FanLowtoMed",0);
        this.currentReadings.put("FanMed",0);
        this.currentReadings.put("FanHigh",0);
        this.currentReadings.put("FanPowerSpike",0);
        this.currentReadings.put("FanPowerError",0);

        this.testResults.put("Step1", "Not tested");
        this.testResults.put("Step2", "Not tested");
        this.testResults.put("Step3", "Not tested");
        this.testResults.put("Step4", "Not tested");
        this.testResults.put("Step5", "Not tested");
        this.testResults.put("Step6", "Not tested");
        this.testResults.put("Step7", "Not tested");
    }
}