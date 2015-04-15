package com.scentair.scentwave;

import com.google.gson.annotations.Expose;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;

public class Phidget {
    @Expose public Integer id;
    public transient InterfaceKitPhidget phidget;
    @Expose public Integer phidgetId;
    @Expose public Integer phidgetSerialNumber;

    public Phidget () {
        this.id = 0;
        try {
            this.phidget = new InterfaceKitPhidget();
        } catch (PhidgetException pe ) {
            pe.printStackTrace();
        }
        this.phidgetId = 0;
        this.phidgetSerialNumber=0;
    }

    @Override
    public String toString() {
        return "phidget [id=" + id +
                "phidget_serial_number=" + phidgetSerialNumber +
                "phidget_id=" + phidgetId +
                "]";
    }
}