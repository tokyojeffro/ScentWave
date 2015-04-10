package com.scentair.scentwave;

import android.support.annotation.NonNull;

import java.util.*;

public class MachineStates{

    private static HashMap<Integer, String> stateMap;

    private class StateRange {
        public Integer min;
        public Integer max;
        public String state;
    }

    public MachineStates() {

        stateMap = new HashMap<Integer, String>();

        StateRange initRange= new StateRange();
        // Unplugged range
        initRange.min=0;
        initRange.max=9;
        initRange.state="Unplugged";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //OffPlugged range
        initRange.min=10;
        initRange.max=19;
        initRange.state="Off";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Machine On, Backlight Off, Fan Off range
        initRange.min=20;
        initRange.max=29;
        initRange.state="BackLight Off";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Machine On, Backlight On, Fan Off range
        initRange.min=30;
        initRange.max=45;
        initRange.state="BackLight On";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Machine On, Fan powering up from off range
        initRange.min=46;
        initRange.max=130;
        initRange.state="FanTurnOn";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan at low range
        initRange.min=131;
        initRange.max=200;
        initRange.state="Low";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan between low and med range
        initRange.min=201;
        initRange.max=220;
        initRange.state="Low to Medium";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan medium range
        initRange.min=221;
        initRange.max=350;
        initRange.state="Medium";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan high range
        initRange.min=351;
        initRange.max=600;
        initRange.state="High";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan spike range
        initRange.min=601;
        initRange.max=650;
        initRange.state="Fan Power Spike";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan power error range
        initRange.min=651;
        initRange.max=1000;
        initRange.state="Fan Power Error";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }
    }

    public String getState (int currentValue) {
        return stateMap.get(currentValue);
    }
};