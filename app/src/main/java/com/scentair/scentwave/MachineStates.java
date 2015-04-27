package com.scentair.scentwave;

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
        // Recalibrate range
        initRange.min=-999;
        initRange.max=-6;
        initRange.state="Recalibrate";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        // Unplugged range
        initRange.min=-5;
        initRange.max=9;
        initRange.state="Unplugged";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Machine On, Backlight Off, Fan Off range
        initRange.min=10;
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

        //Fan medium to high range
        initRange.min=351;
        initRange.max=400;
        initRange.state="Medium to High";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan high range
        initRange.min=401;
        initRange.max=579;
        initRange.state="High";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan spike range
        initRange.min=580;
        initRange.max=999;
        initRange.state="Fan OverLoad";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

        //Fan spike range
        initRange.min=1000;
        initRange.max=1500;
        initRange.state="Error";

        for (int i=initRange.min;i<=initRange.max;i++) {
            stateMap.put(i,initRange.state);
        }

    }

    public String getState (int currentValue) {
        return stateMap.get(currentValue);
    }
};