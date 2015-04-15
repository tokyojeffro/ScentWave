package com.scentair.scentwave;

public class Bay {
    public Boolean active;
    public Integer bayNumber;
    public Integer calibrationOffset;
    public Integer rawValue;

    public Bay(Integer bayNumber) {
        this.active = true;
        this.bayNumber = bayNumber;
        this.calibrationOffset = 0;
        this.rawValue = 0;
    }

    public Bay (Integer bayNumber,Boolean status,Integer offset) {
        this.active = status;
        this.bayNumber = bayNumber;
        this.calibrationOffset = offset;
        this.rawValue = 0;
    }

    public Integer getCalibrationOffset(){
        return calibrationOffset;
    }

    public void setCalibrationOffset(Integer newOffset) {
        this.calibrationOffset=newOffset;
    }

}