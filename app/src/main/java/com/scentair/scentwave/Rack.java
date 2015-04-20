package com.scentair.scentwave;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;

public class Rack {
    public Integer number;
    public Phidget[] phidgets;
    public Bay[] bays;
    public final Integer numberOfBays=24;
    public final Integer numberOfPhidgetsPerRack=numberOfBays/8;

    private static final String TAG_RACK_NUMBER = "rackNumber";
    private static final String TAG_PHIDGET_RACK_NUMBER = "rackNumber";
    private static final String TAG_BAY_STATUS = "active";
    private static final String TAG_BAY_NUMBER = "bayNumber";
    private static final String TAG_CALIBRATION_OFFSET = "calibrationOffset";
    private static final String TAG_PHIDGET_NUMBER = "phidgetId";
    private static final String TAG_PHIDGET_SERIAL_NUMBER = "phidgetSerialNumber";
    private static final String TAG_PHIDGET_ID = "id";

    public Rack (Integer number, String serverAddress) {
        this.number = number;

        JSONArray json_operators;

        phidgets = new Phidget[numberOfPhidgetsPerRack];
        for (int i=0;i<numberOfPhidgetsPerRack;i++) {
            Phidget phidget = new Phidget(this.number);
            phidgets[i]= phidget;
        }
        bays = new Bay[numberOfBays];

        // Get the JSON for the assigned rack
        String url = "http://" + serverAddress + "/dbtest.php/rackbays";
        JSONParser jParser = new JSONParser();
        json_operators = jParser.getJSONFromUrl(url);

        try {
            // looping through all operators
            for (int i = 0; i < json_operators.length();i++)
            {
                JSONObject q = json_operators.getJSONObject(i);

                Boolean bayStatus = false;
                Integer tempInt = q.getInt(TAG_BAY_STATUS);
                if (tempInt!=0) bayStatus=true;

                Integer offset = q.getInt(TAG_CALIBRATION_OFFSET);
                Integer bayNumber = q.getInt(TAG_BAY_NUMBER);
                Integer rackNumber = q.getInt(TAG_RACK_NUMBER);
                Integer id = q.getInt(TAG_PHIDGET_ID);

                // Only save the info related to this bay from the table.
                if (rackNumber.equals(this.number)) {
                    Bay newBay = new Bay(this.number, bayNumber, bayStatus, offset, id);
                    // Storing each json item in variables
                    bays[bayNumber-1] = newBay;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get the JSON for the assigned rack
        url = "http://" + serverAddress + "/dbtest.php/rackphidgets";

        jParser = new JSONParser();

        json_operators = jParser.getJSONFromUrl(url);

        try {
            // looping through all operators
            for (int i = 0; i < json_operators.length(); i++)
            {
                JSONObject q = json_operators.getJSONObject(i);

                Integer phidgetNumber = q.getInt(TAG_PHIDGET_NUMBER);
                Integer phidgetSerialNumber = q.getInt(TAG_PHIDGET_SERIAL_NUMBER);
                Integer rackNumber = q.getInt(TAG_PHIDGET_RACK_NUMBER);
                Integer id = q.getInt(TAG_PHIDGET_ID);

                // Only save info related to this rack.
                if (rackNumber.equals(this.number)) {
                    phidgets[phidgetNumber-1].phidgetSerialNumber = phidgetSerialNumber;
                    phidgets[phidgetNumber-1].id = id;
                    phidgets[phidgetNumber-1].phidgetId = phidgetNumber;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Bay[] getBays() {
        return bays;
    }

    public Integer getActiveBays() {
        Integer activeBays = 0;

        for (int i=0; i<bays.length; i++) {
            if (bays[i].active) activeBays++;
        }
        return activeBays;
    }

    public Rack getRack() {
        return this;
    }

    public void updateCalibrationTables (String serverAddress) {
        InputStream inputStream = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "http://" + serverAddress + "/dbtest.php/rackphidgets";

        HttpPost httpPostReq = new HttpPost(url);

        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        String jsonOutput=gson.toJson(this.phidgets);

        StringEntity se;

        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            inputStream = httpResponse.getEntity().getContent();
            String text = inputStream.toString();
            Log.i("Response received", text);

        } catch (Exception e) {
            e.printStackTrace();
        }

        httpClient = new DefaultHttpClient();
        url = "http://" + serverAddress + "/dbtest.php/rackbays";

        httpPostReq = new HttpPost(url);

        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        jsonOutput=gson.toJson(this.bays);

        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            inputStream = httpResponse.getEntity().getContent();
            String text = inputStream.toString();
            Log.i("Response received", text);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}