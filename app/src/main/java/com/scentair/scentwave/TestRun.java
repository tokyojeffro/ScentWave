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
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class TestRun{
    public Integer overallUnitsFailed=0;
    public Integer currentStepUnitsTested=0;
    public Integer currentStepUnitsPassed=0;
    public Integer currentStepUnitsFailed=0;
    public Integer currentTestStep=1;
    public Integer currentBay=0;
    public Integer maxTestSteps;
    public Integer numberOfActiveBays;
    public BayItem[] bayItems;
    public TestResult testResult;
    // Constructor to generate a new test run
    public TestRun(String operator, Rack rack, Integer numTestSteps){
        this.maxTestSteps=numTestSteps;
        this.overallUnitsFailed = 0;
        this.numberOfActiveBays=rack.getActiveBays();
        this.testResult = new TestResult(operator, rack.number, this.numberOfActiveBays, numTestSteps);
        // Need to keep all bays included here, even though some may be inactive
        this.bayItems = new BayItem[rack.numberOfBays];
        for(int i=0;i<rack.numberOfBays;i++){
            boolean status = rack.bays[i].active;
            bayItems[i]=new BayItem(i+1, status);
        }
        // The first step is to enter the barcodes.  Set the edit field
        this.bayItems[0].isEditMitec=true;
        // Set the start time for the first test step
        this.testResult.setStartTime(0);
    }
    // Empty constructor for reconstitution after resume
    public TestRun () {}

    public TestRun getTestRun(){
        return this;
    }
    public Integer getNextActiveBay (Integer position) {
        Integer returnValue=position+1;
        Boolean activeFound=false;

        //Loops through to find the next active bay after the given position
        while (returnValue<bayItems.length && !activeFound) {
            if (bayItems[returnValue].isActive) activeFound=true;
            else returnValue++;
        }
        if (!activeFound) {
            // There were no more active bays left
            // Return 24 to move to bottom of list
            return bayItems.length;
        } else return returnValue;
    }
    public Integer setNextBarcodeEditField () {
        Integer targetBay = -1;
        // Look at the data and figure out where to put the cursor for barcode entry
        // First, check the field we are starting on.  Is is active and does it have both barcode values already?
        // If not, then we stay there.
        // Need to go through the active list looking for the next target field
        // Should clear each field before setting the target
        for (int i=0;i<bayItems.length;i++) {
            bayItems[i].isEditScentair = false;
            bayItems[i].isEditMitec = false;
            // If the bay is active and we have not found the target bay yet, then check the fields
            if ((bayItems[i].isActive && targetBay.equals(-1))) {
                if (bayItems[i].mitecBarcode.equals("")) {
                    // Put the pointer here
                    bayItems[i].isEditMitec = true;
                    targetBay = i;
                } else if (bayItems[i].scentairBarcode.equals("")) {
                    bayItems[i].isEditScentair = true;
                    targetBay = i;
                }
            }
        }
        return targetBay;
    }
    public void calculateResults(Rack rack) {
        // Each unit in the test needs new unit and unittest objects
        for (int i=0;i<rack.numberOfBays;i++) {
            // Find the next active bay
            if (bayItems[i].isActive) {
                // If there are any active bays left
                BayItem bayItem = bayItems[i];
                // Create a new Unit with the barcode info
                SW1004Unit newUnit = new SW1004Unit(
                        bayItem.mitecBarcode,
                        bayItem.scentairBarcode
                );
                // Get the results from that bay and create a new unit test
                UnitTest newUnitTest = new UnitTest(
                        newUnit,
                        // If it failed, put a FALSE in passed for this test result
                        !bayItem.isFailed,
                        bayItem.bayNumber,
                        bayItem.fanMedDisplayValue,
                        bayItem.highValue,
                        bayItem.medValue,
                        bayItem.lowValue,
                        bayItem.failCause,
                        bayItem.failCauseIndex
                );
                // Add the new UnitTest to the list
                testResult.unitTests.add(newUnitTest);
            }
        }
        testResult.numberOfUnitsFailed = overallUnitsFailed + currentStepUnitsFailed;
        testResult.numberOfUnitsPassed = numberOfActiveBays - testResult.numberOfUnitsFailed;
        if (testResult.numberOfUnitsFailed>=numberOfActiveBays) {
            // Special case where all units have failed in this run
            // Set up the data structure for export by filling in the necessary blanks
            for (int i=0;i<maxTestSteps;i++) {
                testResult.setStartTime(i);
                testResult.setEndTime(i);
            }
        }
        // This structure is necessary for serialization and output to the database
        testResult.step1Start = testResult.stepStartTimes[0];
        testResult.step2Start = testResult.stepStartTimes[1];
        testResult.step3Start = testResult.stepStartTimes[2];
        testResult.step4Start = testResult.stepStartTimes[3];
        testResult.step5Start = testResult.stepStartTimes[4];
        testResult.step1Stop = testResult.stepEndTimes[0];
        testResult.step2Stop = testResult.stepEndTimes[1];
        testResult.step3Stop = testResult.stepEndTimes[2];
        testResult.step4Stop = testResult.stepEndTimes[3];
        testResult.step5Stop = testResult.stepEndTimes[4];
    }
    public void saveTestResults (String serverAddress) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // First step, save the new units in their table
        String url = "http://" + serverAddress + "/dbtest.php/testresults";
        HttpPost httpPostReq = new HttpPost(url);
        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String jsonOutput=gson.toJson(this.testResult);
        StringEntity se;
        String httpResponseText="";
        Integer testRunId = 0;
        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);
            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            BufferedReader in = null;
            try {
                //Log.d("status line ", "test " + response.getStatusLine().toString());
                in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                httpResponseText =  sb.toString();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("Response received", httpResponseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Need to extract the ID of the Test Result to include in the unit test update
        try {
            JSONObject postResponseObject = new JSONObject(httpResponseText);
            testRunId = postResponseObject.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Now add the units to the database with a JSON POST
        httpClient = new DefaultHttpClient();
        // This is the url to update the 1004 unit table
        url = "http://" + serverAddress + "/dbtest.php/sw1004units";
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        // Need to get the unit info into JSON format
        SW1004Unit newUnits[] = new SW1004Unit[testResult.unitTests.size()];
        for (int i=0;i<testResult.unitTests.size();i++)
            newUnits[i] = testResult.unitTests.get(i).unit;
        jsonOutput=gson.toJson(newUnits);
        httpPostReq = new HttpPost(url);
        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");
        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            BufferedReader in = null;
            try {
                //Log.d("status line ", "test " + response.getStatusLine().toString());
                in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                httpResponseText =  sb.toString();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("Response received", httpResponseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // The response should be a JSON array of units inserted.
        // Parse the array and save off the new ID values back into the Unit objects
        try {
            JSONArray responseArray = new JSONArray(httpResponseText);

            for (int i=0;i<responseArray.length();i++) {
                JSONObject q = responseArray.getJSONObject(i);
                testResult.unitTests.get(i).unit.id = q.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Finally, add the units to the unit test table.
        // It joins the Unit IDs with the Test Run Ids and stores
        // the test run specific data.
        httpClient = new DefaultHttpClient();
        // This is the url to update the 1004 unit table
        url = "http://" + serverAddress + "/dbtest.php/unittests";
        // Generate the proper data
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        jsonOutput=gson.toJson(testResult.unitTests);
        httpPostReq = new HttpPost(url);
        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");
        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            BufferedReader in = null;
            try {
                //Log.d("status line ", "test " + response.getStatusLine().toString());
                in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                httpResponseText =  sb.toString();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("Response received", httpResponseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Need to extract the ID of the Test Result to include in the unit test update
        try {
            JSONArray responseArray = new JSONArray(httpResponseText);

            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject q = responseArray.getJSONObject(i);
                testResult.unitTests.get(i).unitTestId = q.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // For the last step, write out the linking table records from the data we have parsed and stored
        // First, build the link record objects
        ArrayList<DataLinkRecord> linkRecords = new ArrayList<DataLinkRecord>();

        // For each Unit Test, build a new link record
        Iterator<UnitTest> iterator = testResult.unitTests.iterator();
        while (iterator.hasNext()) {
            UnitTest nextTest = iterator.next();
            Integer unitTestId = nextTest.unitTestId;
            Integer unitId = nextTest.unit.id;
            DataLinkRecord newRecord = new DataLinkRecord(testRunId,unitTestId,unitId);
            linkRecords.add(newRecord);
        }
        // Now, output to the database
        httpClient = new DefaultHttpClient();
        // This is the url to update the 1004 unit table
        url = "http://" + serverAddress + "/dbtest.php/testruns_unittests";
        // Generate the proper data
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        jsonOutput=gson.toJson(linkRecords);
        httpPostReq = new HttpPost(url);
        httpPostReq.setHeader("Accept", "application/json");
        httpPostReq.setHeader("Content-type","application/json");
        try {
            se = new StringEntity(jsonOutput);
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httpPostReq.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPostReq);
            BufferedReader in = null;
            try {
                //Log.d("status line ", "test " + response.getStatusLine().toString());
                in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                httpResponseText =  sb.toString();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("Response received", httpResponseText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}