package com.scentair.scentwave;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.scentair.scentwave.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity {
    InterfaceKitPhidget ik;

    TextView[] sensorsTextViews;
    CheckBox[] inputCheckBoxes;
    Integer SampleNumber=1;
    TextView sampleTextView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorsTextViews = new TextView[8];
        sensorsTextViews[0] = (TextView)findViewById(R.id.sensor0);
        sensorsTextViews[1] = (TextView)findViewById(R.id.sensor1);
        sensorsTextViews[2] = (TextView)findViewById(R.id.sensor2);
        sensorsTextViews[3] = (TextView)findViewById(R.id.sensor3);
        sensorsTextViews[4] = (TextView)findViewById(R.id.sensor4);
        sensorsTextViews[5] = (TextView)findViewById(R.id.sensor5);
        sensorsTextViews[6] = (TextView)findViewById(R.id.sensor6);
        sensorsTextViews[7] = (TextView)findViewById(R.id.sensor7);

        //baby steps.  Get the sample number associated with the variable
        sampleTextView=(TextView)findViewById(R.id.sampleTxt);
        sampleTextView.setText(String.valueOf(SampleNumber));

        // Doing this to make the compiler happy
        inputCheckBoxes = new CheckBox[8];

        try
        {
            ik = new InterfaceKitPhidget();
            ik.addAttachListener(new AttachListener() {
                public void attached(final AttachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), true);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addDetachListener(new DetachListener() {
                public void detached(final DetachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), false);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addSensorChangeListener(new SensorChangeListener() {
                public void sensorChanged(SensorChangeEvent se) {
                    runOnUiThread(new SensorChangeRunnable(se.getIndex(), se.getValue()));
                }
            });
            ik.addInputChangeListener(new InputChangeListener() {
                public void inputChanged(InputChangeEvent ie) {
                    runOnUiThread(new InputChangeRunnable(ie.getIndex(), ie.getState()));
                }


        });
            ik.openAny("192.168.1.22", 5001);
        }
        catch (PhidgetException pe)
        {
            pe.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ik.close();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }

    class OnOutputClickListener implements CheckBox.OnClickListener {
        InterfaceKitPhidget phidget;
        int index = 0;
        public OnOutputClickListener(InterfaceKitPhidget phidget, int index)
        {
            this.phidget = phidget;
            this.index = index;
        }
        @Override
        public void onClick(View v) {
            try {
                if(phidget.isAttached()){
                    // Perform action on clicks, depending on whether it's now checked
                    if (((CheckBox) v).isChecked()) {
                        phidget.setOutputState(index, true);
                    } else {
                        phidget.setOutputState(index, false);
                    }
                }
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }

    }

    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
        }
        public void run() {
            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);
            if(attach)
            {
                attachedTxt.setText("Attached");
                try {
                    TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
                    TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
                    TextView versionTxt = (TextView) findViewById(R.id.versionTxt);

                    nameTxt.setText(phidget.getDeviceName());
                    serialTxt.setText(Integer.toString(phidget.getSerialNumber()));
                    versionTxt.setText(Integer.toString(phidget.getDeviceVersion()));

                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
            else
                attachedTxt.setText("Detached");
            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class SensorChangeRunnable implements Runnable {
        int sensorIndex, sensorVal;
        public SensorChangeRunnable(int index, int val)
        {
            SampleNumber++;
            this.sensorIndex = index;
            this.sensorVal = val;
        }
        public void run() {
            if(sensorsTextViews[sensorIndex]!=null)
                sensorsTextViews[sensorIndex].setText(sensorIndex +": "+sensorVal);
            sampleTextView.setText(String.valueOf(SampleNumber));
        }
    }

    class InputChangeRunnable implements Runnable {
        int index;
        boolean val;
        public InputChangeRunnable(int index, boolean val)
        {
            this.index = index;
            this.val = val;
        }
        public void run() {
            if(inputCheckBoxes[index]!=null)
                inputCheckBoxes[index].setChecked(val);
        }
    }
}