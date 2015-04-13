package com.scentair.scentwave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TestSteps {

    private static final String TAG_TEST_STEPS = "testSteps";
    private static final String TAG_EXPECTED_RESULTS = "expectedResults";
    private static final String TAG_TEST_STEP_ID = "testStepId";
    private static final String TAG_FAILURE_ID = "failureId";

    private static ArrayList<TestStepFailure> testStepFailures;

    public ArrayList<TestStep> testSteps;

    JSONArray json_operators = null;

    // Constructor
    public TestSteps (String dbAddress) {
        // Read the array of tests from the database on the network

        //Initialize the operator array list
        testSteps = new ArrayList<TestStep>();

        // Get the JSON
        String url = "http://" + dbAddress + "/dbtest.php/tests";

        JSONParser jParser = new JSONParser();

        json_operators = jParser.getJSONFromUrl(url);

        try {
            // looping through all quakes
            for (int i = 0; i < json_operators.length(); i++)
            {
                TestStep testStep= new TestStep();
                JSONObject q = json_operators.getJSONObject(i);
                // Storing each json item in variables
                testStep.testSteps=q.getString(TAG_TEST_STEPS);
                testStep.expectedResults=q.getString(TAG_EXPECTED_RESULTS);

                testSteps.add(testStep);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
        }

        // Also need to load the array of possible failures per test step
        testStepFailures = new ArrayList<TestStepFailure>();

        // Get the JSON
        url = "http://" + dbAddress + "/dbtest.php/teststepfailures";

        json_operators = jParser.getJSONFromUrl(url);

        try {
            // looping through all quakes
            for (int i = 0; i < json_operators.length(); i++)
            {
                TestStepFailure testStepFailure= new TestStepFailure();
                JSONObject q = json_operators.getJSONObject(i);
                // Storing each json item in variables
                testStepFailure.testStepId=q.getInt(TAG_TEST_STEP_ID);
                testStepFailure.failureId=q.getInt(TAG_FAILURE_ID);
                testStepFailures.add(testStepFailure);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
        }
        // Finally, need to load up the possible failures for each teach case
        for (int i=0;i<testStepFailures.size();i++) {
            // Set up two new local varaibles
            TestStep testStep = new TestStep();
            TestStepFailure testStepFailure = new TestStepFailure();

            // point those variables at the right memory spots in our lists
            testStepFailure = testStepFailures.get(i);
            testStep = testSteps.get(testStepFailure.testStepId-1);

            // add a new failure case for that test step
            testStep.possibleFailures.add(testStepFailure.failureId);
        }

    }

    public ArrayList<TestStep> getTestSteps() {
        return testSteps;
    }
    public TestStep getTestStep (Integer testStepNum) {
        return testSteps.get(testStepNum);
    }

    public Integer getMaxSteps () {
        return testSteps.size();
    }


}