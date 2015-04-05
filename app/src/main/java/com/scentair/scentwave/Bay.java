package com.scentair.scentwave;

public class Bay {
    public Boolean active;
    public Integer bayNumber;
    public Integer calibrationOffset;

    public Bay(Integer bayNumber) {
        active = true;
        this.bayNumber = bayNumber;
        calibrationOffset = 0;
    }

    public Integer getCalibrationOffset(){
        return calibrationOffset;
    }

    public void setCalibrationOffset(Integer newOffset) {
        this.calibrationOffset=newOffset;
    }

}