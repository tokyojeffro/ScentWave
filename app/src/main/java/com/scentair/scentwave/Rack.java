package com.scentair.scentwave;

import java.util.ArrayList;

public class Rack {
    public Integer number;
    public Integer[] phidgetSerialNumbers;
    static public Bay[] bays;

    public Rack (Integer number, Integer[] phidgets) {
        this.number = number;

        phidgetSerialNumbers = new Integer[phidgets.length];

        for (int i = 0; i < phidgets.length; i++) {
            phidgetSerialNumbers[i] = phidgets[i];
        }

        bays = new Bay[24];

        for (int i = 0; i < 24; i++) {
            bays[i] = new Bay(i+1);
        }
    }

    public Bay[] getBays() {
        return bays;
    }
}