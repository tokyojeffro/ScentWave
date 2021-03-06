package com.scentair.scentwave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Manager;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.scentair.scentwave.CalibrationBayArrayAdapter.customCalibrationButtonListener;
import java.util.Vector;

public class CalibrationActivity extends Activity implements customCalibrationButtonListener {
    public Rack rack;
    ListView listView;
    public CalibrationBayArrayAdapter aa;
    Context context;
    private Button toggleRackButton;
    private String phidgetServerAddress;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Integer currentRack;
    public Vector<InterfaceKitPhidget> phidgets;
    private Resources resources;
    private Integer numberOfRacks;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration);
        context = this.getApplicationContext();
        View footerView;
        // Initialize the non-volatile storage area
        sharedPreferences = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE);
        // Pull the associated rack number from NVM
        currentRack = sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER, 0);
        phidgetServerAddress = sharedPreferences.getString(MainActivity.TAG_PHIDGET_SERVER_ADDRESS, "192.168.1.22");
        //Need to build out the bay list here.
        //The bay list is a set of fragments attached to a special adapter
        listView = (ListView) findViewById(R.id.calibrate_list_view);
        footerView =  ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.calibration_footer, null, false);
        listView.addFooterView(footerView);
        resources = getResources();
        numberOfRacks = resources.getInteger(R.integer.NUMBER_OF_RACKS);
        //Crank up the async process to load the rack values from the DB
        new loadDBValues().execute("http://this string argument does nothing");
        // Used in case we change the rack or the serial numbers here in calibration
        editor = getSharedPreferences(MainActivity.TAG_MYPREFS, Context.MODE_PRIVATE).edit();
        toggleRackButton = (Button) findViewById(R.id.current_rack);
        toggleRackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRack++;
                if (currentRack>numberOfRacks) currentRack=1;
                // Save in NVM
                editor.putInt(MainActivity.TAG_RACK_NUMBER, currentRack);
                editor.commit();
                // Trigger a reload for the rack values from the DB
                new loadDBValues().execute("http://this string argument does nothing");
            }
        });
        TextView phidget1Field = (TextView) findViewById(R.id.phidget_1);
        phidget1Field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePhidget1();
            }
        });
        TextView phidget2Field = (TextView) findViewById(R.id.phidget_2);
        phidget2Field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePhidget2();
            }
        });
        TextView phidget3Field = (TextView) findViewById(R.id.phidget_3);
        phidget3Field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePhidget3();
            }
        });
        Button saveAndExitButton = (Button) findViewById(R.id.save_and_exit_button);
        saveAndExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });
    }
    @Override
    public void onToggleButtonClickListener(int position, int listViewPosition) {
        // Toggle the active status of this bay
        rack.bays[position].active = !rack.bays[position].active;
        updateView();
    }
    @Override
    public void onCalibrationIncrementButtonClickListener(int position, int listViewPosition) {
        // Increment the calibration offset value
        rack.bays[position].calibrationOffset++;
        updateView();
    }
    @Override
    public void onCalibrationDecrementButtonClickListener(int position, int listViewPosition) {
        // Increment the calibration offset value
        rack.bays[position].calibrationOffset--;
        updateView();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect and shut down all the phidgets and services.
        try {
            for (int i = 0; i < rack.numberOfPhidgetsPerRack; i++) {
                rack.phidgets[i].phidget.close();
            }
        }
        catch (PhidgetException pe) {
            pe.printStackTrace();
        }
    }
    private void updateView() {
        //TestStep testStep = testSteps.get(testRun.currentTestStep-1);
        //Get the header info loaded from the data structure
        String text = currentRack.toString();
        toggleRackButton.setText(text);

        // Set up phidget serial number 1
        TextView phidgetSerialView = (TextView) findViewById(R.id.phidget_1);
        text = rack.phidgets[0].phidgetSerialNumber.toString();
        phidgetSerialView.setText(text);
        try {
            if (rack.phidgets[0].phidget.isAttached()) {
                // Set background color to green
                phidgetSerialView.setBackgroundColor(Color.GREEN);
            } else {
                phidgetSerialView.setBackgroundColor(Color.LTGRAY);
            }
        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }
        // Set up phidget serial number 2
        phidgetSerialView = (TextView) findViewById(R.id.phidget_2);
        text = rack.phidgets[1].phidgetSerialNumber.toString();
        phidgetSerialView.setText(text);
        try {
            if (rack.phidgets[1].phidget.isAttached()) {
                // Set background color to green
                phidgetSerialView.setBackgroundColor(Color.GREEN);
            } else {
                phidgetSerialView.setBackgroundColor(Color.LTGRAY);
            }
        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }
        // Set up phidget serial number 3
        phidgetSerialView = (TextView) findViewById(R.id.phidget_3);
        text = rack.phidgets[2].phidgetSerialNumber.toString();
        phidgetSerialView.setText(text);
        try {
            if (rack.phidgets[2].phidget.isAttached()) {
                // Set background color to green
                phidgetSerialView.setBackgroundColor(Color.GREEN);
            } else {
                phidgetSerialView.setBackgroundColor(Color.LTGRAY);
            }
        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }
        // Turn on the led for each active bay. turn it off for inactive bays
        for (int i=0;i<rack.numberOfBays;i++) {
            updateLED(i, rack.bays[i].active);
        }
        aa.notifyDataSetChanged();
    }
    private void saveAndExit() {
        new saveDBValues().execute("http://this is a test");
        // Clear out any saved test runs because you can't reconcile the lists across bays.
        editor.putString(TestRunActivity.TAG_SAVED_TEST_RUN,"");
        editor.putBoolean(MainActivity.TAG_RESUME_AVAILABLE, false);
        editor.commit();
        finish();
    }
    private void updatePhidget1() {
        LayoutInflater inflater= getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.phidget_list,null);

        final CharSequence[] phidgetList = new CharSequence[phidgets.size()];
        try {
            for (int i = 0; i < phidgets.size(); i++) {
                Phidget newPhidget = phidgets.get(i);
                Integer serialNumber = newPhidget.getSerialNumber();
                phidgetList[i] = serialNumber.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(CalibrationActivity.this);
        builder.setTitle("Available Phidgets")
                .setItems(phidgetList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Load the proper failure reason into the failure field
                        String newPhidgetNumberString=phidgetList[which].toString();
                        rack.phidgets[0].phidgetSerialNumber=Integer.parseInt(newPhidgetNumberString);
                        aa.notifyDataSetChanged();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateView();
                    }
                })
                .setView(dialogView)
                .show();
    }
    private void updatePhidget2() {
        LayoutInflater inflater= getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.phidget_list,null);

        final CharSequence[] phidgetList = new CharSequence[phidgets.size()];
        try {
            for (int i = 0; i < phidgets.size(); i++) {
                Phidget newPhidget = phidgets.get(i);
                Integer serialNumber = newPhidget.getSerialNumber();
                phidgetList[i] = serialNumber.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(CalibrationActivity.this);
        builder.setTitle("Available Phidgets")
                .setItems(phidgetList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Load the proper failure reason into the failure field
                        String newPhidgetNumberString=phidgetList[which].toString();
                        rack.phidgets[1].phidgetSerialNumber=Integer.parseInt(newPhidgetNumberString);
                        aa.notifyDataSetChanged();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateView();
                    }
                })
                .setView(dialogView)
                .show();
    }
    private void updatePhidget3() {
        LayoutInflater inflater= getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.phidget_list,null);

        final CharSequence[] phidgetList = new CharSequence[phidgets.size()];
        try {
            for (int i = 0; i < phidgets.size(); i++) {
                Phidget newPhidget = phidgets.get(i);
                Integer serialNumber = newPhidget.getSerialNumber();
                phidgetList[i] = serialNumber.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(CalibrationActivity.this);
        builder.setTitle("Available Phidgets")
                .setItems(phidgetList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Load the proper failure reason into the failure field
                        String newPhidgetNumberString=phidgetList[which].toString();
                        rack.phidgets[2].phidgetSerialNumber=Integer.parseInt(newPhidgetNumberString);
                        aa.notifyDataSetChanged();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateView();
                    }
                })
                .setView(dialogView)
                .show();
    }
    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach) {
            this.phidget = phidget;
            this.attach = attach;
            Integer sensorChangeTrigger = resources.getInteger(R.integer.PHIDGET_SENSOR_CHANGE_TRIGGER);
            Integer dataRate = resources.getInteger(R.integer.PHIDGET_DATA_RATE);
            Boolean ratioMetric = resources.getBoolean(R.bool.PHIDGET_RATIO_METRIC);
            try {
                if (phidget.isAttached()) {
                    if (phidget==rack.phidgets[0].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[0].phidget.setDataRate(i, dataRate);
                            rack.phidgets[0].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[0].phidget.setRatiometric(false);
                    } else if (phidget==rack.phidgets[1].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[1].phidget.setDataRate(i, dataRate);
                            rack.phidgets[1].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[1].phidget.setRatiometric(false);
                    } else if (phidget==rack.phidgets[2].phidget) {
                        for (int i = 0; i < 8; i++) {
                            rack.phidgets[2].phidget.setDataRate(i, dataRate);
                            rack.phidgets[2].phidget.setSensorChangeTrigger(i, sensorChangeTrigger);
                        }
                        rack.phidgets[2].phidget.setRatiometric(ratioMetric);
                    }
                }
            } catch (PhidgetException pe) {
                pe.printStackTrace();
            }
        }
        public void run() {
            //notify that we're done
            synchronized (this) {
                this.notify();
                updateView();
            }
        }
    }
    class SensorChangeRunnable implements Runnable {
        int phidgetNumber,sensorIndex, sensorVal;
        public SensorChangeRunnable(int phidgetNumber, int index, int val) {
            this.sensorIndex = index;
            this.sensorVal = val;
            this.phidgetNumber=phidgetNumber;
        }
        public void run() {
            Integer bayValue = (phidgetNumber*8)+sensorIndex;
            rack.bays[bayValue].rawValue = sensorVal;
            aa.notifyDataSetChanged();
        }
    }
    private void updateLED (Integer bayNumber, Boolean turnOn) {
        // This function figures out the correct phidget and offset, then sets the toggle value
        Integer phidgetOffset = bayNumber/8;
        Integer phidgetSensorNumber = bayNumber - phidgetOffset*8;
        Phidget thisPhidget= rack.phidgets[phidgetOffset].phidget;
        try {
            if(thisPhidget.isAttached()){
                // Perform action on clicks, depending on whether it's now checked
                rack.phidgets[phidgetOffset].phidget.setOutputState(phidgetSensorNumber,turnOn);
            }
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }
    private class saveDBValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            rack.updateCalibrationTables(MainActivity.dbServerAddress);
            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Calibration updated", Toast.LENGTH_LONG).show();
        }
    }
    private class loadDBValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Integer currentRack=sharedPreferences.getInt(MainActivity.TAG_RACK_NUMBER,1);
            rack = new Rack(currentRack,MainActivity.dbServerAddress);
            return urls[0];
        }
        @Override
        protected void onPostExecute(String result) {
            // Continue setup after we have loaded the rack info from the DB.
            Manager phidgetManager;
            aa = new CalibrationBayArrayAdapter(context, rack.getBays());
            aa.setCustomCalibrationButtonListener(CalibrationActivity.this);
            listView.setAdapter(aa);
            // add the phidget interface stuff for the real time value.
            try {
                try {
                    phidgetManager = new Manager();
                    phidgetManager.open(phidgetServerAddress,5001);

                    phidgets =phidgetManager.getPhidgets();

                } catch (PhidgetException pe) {
                    pe.printStackTrace();
                }
                for (int i=0;i<rack.numberOfPhidgetsPerRack;i++) {
                    rack.phidgets[i].phidget.addAttachListener(new AttachListener() {
                        public void attached(final AttachEvent ae) {
                            AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), true);
                            synchronized (handler) {
                                runOnUiThread(handler);
                                try {
                                    handler.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    rack.phidgets[i].phidget.addDetachListener(new DetachListener() {
                        public void detached(final DetachEvent ae) {
                            AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), false);
                            synchronized (handler) {
                                runOnUiThread(handler);
                                try {
                                    handler.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    final int finalI = i;
                    rack.phidgets[i].phidget.addSensorChangeListener(new SensorChangeListener() {
                        public void sensorChanged(SensorChangeEvent se) {
                            runOnUiThread(new SensorChangeRunnable(finalI, se.getIndex(), se.getValue()));
                        }
                    });
                    rack.phidgets[i].phidget.open(rack.phidgets[i].phidgetSerialNumber, phidgetServerAddress, 5001);
                }
            } catch (PhidgetException pe) {
                pe.printStackTrace();
            }
            updateView();
        }
    }
}