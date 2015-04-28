package com.scentair.scentwave;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BayItem{
    static final long ONE_MINUTE_IN_MILLIS=60000;
    public Integer bayNumber;
    public String mitecBarcode;
    public String scentairBarcode;
    public String unitState;
    // just setting this to something unusual for default.  -50 should never happen
    public Integer currentValue=-50;
    public String stepStatus;
    public Boolean isFailed;
    public String failCause;
    public Integer failCauseIndex;
    public Integer failStep;
    public Boolean isEditMitec;
    public Boolean isEditScentair;
    public Boolean isActive;
    public Integer lowValue = 0;
    private Date lowValueTimestamp;
    public Integer medValue = 0;
    private Date medValueTimestamp;
    public Integer highValue = 0;
    private Date highValueTimestamp;
    public String fanMedDisplayValue;
    public Boolean cycleTestComplete=false;
    public Date lastOffTime;
    private String oldUnitState;
    public String lcdState;
    private Integer oldCurrentValue;

    //Constructor used for beginning a test run
    public BayItem (Integer bayNumber, boolean activeStatus) {
        this.bayNumber=bayNumber;
        this.mitecBarcode = "";
        this.scentairBarcode = "";
        this.currentValue = -50;
        this.oldCurrentValue= -50;
        this.stepStatus = "Not Tested";
        this.unitState = "Unplugged";
        this.oldUnitState = "Unplugged";
        this.failCause = "";
        this.failStep = 0;
        this.failCauseIndex=0;
        this.isEditMitec=false;
        this.isEditScentair=false;
        this.isActive = activeStatus;
        this.isFailed=false;
        this.lowValue=0;
        this.medValue=0;
        this.highValue=0;
        this.lastOffTime = new Date();
        this.cycleTestComplete=false;
        this.fanMedDisplayValue="";
        this.lcdState="OFF";
    }

    public String isPassReady(Integer testNumber) {
        // This is the logic that figures out whether a bay is ready to activate the Pass button
        // and move on to the next step
        // If the return value is not null, the string indicates the pending status before pass is enabled.
        String returnValue = "Error";

        switch (testNumber) {
            case 1:
                // Pass criteria for step 1 is both barcodes are entered.
                // Data validation happens upon data entry, so we just need to check that both
                // fields have a value here.
                if (!this.mitecBarcode.isEmpty() && !this.scentairBarcode.isEmpty() ) {
                    returnValue = "Passed";
                    this.stepStatus="Passed";
                } else {
                    returnValue="Barcodes not entered";
                    //this.stepStatus = "Not Tested";
                }
                break;
            case 2:
                // Pass criteria for step 2 is the machine has been plugged in and the state is
                // not 'Unplugged' and not recalibrate
                if ( ( !this.unitState.equals("Unplugged")) && (!this.unitState.equals("Recalibrate")) ) {
                    returnValue = "Pass";
                } else returnValue="Machine not plugged in";
                break;
            case 3:
                // Pass criteria for step 3 is that values are recorded for each target fan speed
                // fan values are saved when the unit state is settled on the target fan speed
                if (medValue!=0 && lowValue!=0 && highValue!=0) {
                    returnValue = "Pass";
                } else returnValue = "Fan speeds not recorded";
                break;
            case 4:
                // There is no automatic pass criteria, this step is all operator driven.
                // So always return true.
                returnValue = "Pass";
                break;
            case 5:
                // This is the final automated step.  Pass criteria is that a machine has cycled off->on->off
                // within 2 minutes +/- 1
                // Cycle test complete is set to true upon shift back to Off at the appointed time.
                if (!cycleTestComplete) {
                    long curTimeinMs = lastOffTime.getTime();
                    Date nextOffTime = new Date(curTimeinMs+(2*ONE_MINUTE_IN_MILLIS));
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.US);
                    returnValue = format.format(nextOffTime);
                } else {
                    returnValue = "Passed";
                    this.stepStatus = "Passed";
                }
                break;
        }
        return returnValue;
    }

    public Boolean updateValue(Integer newValue, Integer testStepNumber) {
        oldCurrentValue = currentValue;
        currentValue = newValue;
        oldUnitState = unitState;
        Boolean refreshScreen = false;
        Date newValueUpdateTime = new Date();
        if (isActive && !isFailed) {
            // Only process this if the bay is active in calibration and have not already failed
            // Check to see if our new value has triggered a state change.
            unitState = TestRunActivity.machineStates.getState(newValue);
            // If we are in test step number 3, then start looking for the proper fan values
            if (testStepNumber.equals(3)) {
                if (oldUnitState!=unitState) {
                    // We have made a large shift.  Reset all timers so we wash out any old pass thru values
                    lowValueTimestamp=null;
                    medValueTimestamp=null;
                    highValueTimestamp=null;
                }
                switch (oldUnitState) {
                    // Update the stored trigger values based on the old state.
                    case "Unplugged":
                        break;
                    case "Low":
                        if (lowValueTimestamp==null) {
                            // Get the first timestamp for Low
                            lowValueTimestamp = new Date();
                        } else {
                            // If it isn't the first time through, check the time we have been on this value
                            Long difference = newValueUpdateTime.getTime() - lowValueTimestamp.getTime();
                            if (difference>1000*2) {
                                // If this low value has been active for 2 seconds or more
                                // save the last value stored
                                lowValue = oldCurrentValue;
                                lowValueTimestamp=null;
                                refreshScreen=true;
                            } else {
                                // Reset the timestamp
                                lowValueTimestamp = new Date();
                            }
                        }
                        break;
                    case "Medium":
                        if (medValueTimestamp==null) {
                            // Get the first timestamp for Low
                            medValueTimestamp = new Date();
                        } else {
                            // If it isn't the first time through, check the time we have been on this value
                            Long difference = newValueUpdateTime.getTime() - medValueTimestamp.getTime();
                            if (difference>1000*2) {
                                // If this low value has been active for 2 seconds or more
                                // save the last value stored
                                medValue = oldCurrentValue;
                                medValueTimestamp=null;
                                refreshScreen=true;
                            } else {
                                // Reset the timestamp
                                medValueTimestamp = new Date();
                            }
                        }
                        break;
                    case "High":
                        if (highValueTimestamp==null) {
                            // Get the first timestamp for Low
                            highValueTimestamp = new Date();
                        } else {
                            // If it isn't the first time through, check the time we have been on this value
                            Long difference = newValueUpdateTime.getTime() - highValueTimestamp.getTime();
                            if (difference>1000*2) {
                                // If this low value has been active for 2 seconds or more
                                // save the last value stored
                                highValue = oldCurrentValue;
                                highValueTimestamp=null;
                                refreshScreen=true;
                            } else {
                                // Reset the timestamp
                                highValueTimestamp = new Date();
                            }
                        }
                        break;
                }
            }

            // Check to see if the cycle timer has already passed
            // If so, ignore this
            if (!cycleTestComplete) {
                if (oldUnitState.equals("Low") && (
                        unitState.equals("BackLight Off") ||
                                unitState.equals("BackLight On") ||
                                unitState.equals("Off") ||
                                unitState.equals("FanTurnOn"))) {
                    // We have toggled from Low to Off (in some form)
                    // Save the timestamp info for future reference
                    if (lastOffTime != null) {
                        // Check the difference here
                        Date checkTime = new Date();
                        // Time difference in milliseconds
                        long difference = checkTime.getTime() - lastOffTime.getTime();
                        Integer seconds = (int) (difference / 1000);
                        // in any case, reset the date here so as not to smudge it with breakpoints
                        lastOffTime = new Date();
                        switch (seconds) {
                            case 117:
                            case 118:
                            case 119:
                            case 120:
                            case 121:
                            case 122:
                            case 123:
                                // We have a winner for fan cycle test timing.
                                cycleTestComplete = true;
                                stepStatus = "Passed";
                                lcdState = "OFF";
                                refreshScreen = true;
                                break;
                            default:
                                // No winner
                                cycleTestComplete = false;
                                break;
                        }
                    } else {
                        // Set the reference time
                        lastOffTime = new Date();
                    }
                }
            }
        }
        return refreshScreen;
    }

    @Override
    public String toString() {
        return "BayItem [bayNumber=" + bayNumber +
                "mitecBarcode=" + mitecBarcode +
                "scentairbarcode=" + scentairBarcode +
                "currentValue=" + currentValue +
                "stepStatus=" + stepStatus +
                "failCause=" + failCause +
                "failStep=" + failStep +
                "failCauseIndex=" + failCauseIndex +
                "isEditMitec=" + isEditMitec +
                "isEditScentair=" + isEditScentair +
                "isActive=" + isActive +
                "isFailed=" + isFailed +
                "lowValue=" + lowValue +
                "medValue=" + medValue +
                "highValue=" + highValue +
                "cycleTestComplete" + cycleTestComplete +
                "]";
    }
}