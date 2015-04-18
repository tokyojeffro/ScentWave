package com.scentair.scentwave;

import com.google.gson.annotations.Expose;

public class Bay {
    @Expose public Boolean active;
    @Expose public Integer bayNumber;
    @Expose public Integer calibrationOffset;
    public Integer rawValue;
    @Expose public Integer id;
    @Expose public Integer rackNumber;

    public Bay(Integer bayNumber) {
        this.active = true;
        this.bayNumber = bayNumber;
        this.calibrationOffset = 0;
        this.rawValue = 0;
        this.id = 0;
        this.rackNumber = 0;
    }

    public Bay (Integer rackNumber, Integer bayNumber,Boolean status,Integer offset, Integer id) {
        this.active = status;
        this.bayNumber = bayNumber;
        this.calibrationOffset = offset;
        this.rawValue = 0;
        this.id = id;
        this.rackNumber = rackNumber;
    }

    public Integer getCalibrationOffset(){
        return calibrationOffset;
    }

    public void setCalibrationOffset(Integer newOffset) {
        this.calibrationOffset=newOffset;
    }
}