package com.scentair.scentwave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TestSteps {

    private static final String db_url = "http://192.168.1.26/dbtest.php";
    private static final String TAG_NUMBER_OF_ACTIONS = "numberOfActions";
    private static final String TAG_TEST_STEP_1 = "testStep1";
    private static final String TAG_TEST_STEP_2 = "testStep2";
    private static final String TAG_EXPECTED_RESULTS = "expectedResults";
    private static final String TAG_POSSIBLE_FAILURES = "possibleFailures";
    private static final String TAG_TEST_NUMBER = "TestNumber";

    public ArrayList<TestStep> testSteps;
    //= new ArrayList<TestStep>(4);

    JSONArray json_operators = null;

    // Constructor
    public TestSteps () {


        // Read the array of tests from the database on the network

            //Initialize the operator array list
            testSteps = new ArrayList<TestStep>();

            // Get the JSON
            String url = db_url + "/tests";

            JSONParser jParser = new JSONParser();

            json_operators = jParser.getJSONFromUrl(url);

            try {

                // looping through all quakes
                for (int i = 0; i < json_operators.length(); i++)
                {
                    TestStep testStep= new TestStep();
                    JSONObject q = json_operators.getJSONObject(i);


                    // Storing each json item in variables

                    testStep.testStep2=q.getString(TAG_TEST_STEP_2);
                    testStep.testStep1=q.getString(TAG_TEST_STEP_1);
                    testStep.numberOfActions=q.getInt(TAG_NUMBER_OF_ACTIONS);
                    testStep.expectedResults=q.getString(TAG_EXPECTED_RESULTS);
                    String temp = q.getString(TAG_POSSIBLE_FAILURES);
                    //testStep.possibleFailures=q.getString(TAG_POSSIBLE_FAILURES);

                    testSteps.add(testStep);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
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

    public void setStartTime (int position) {
        TestStep testStep = testSteps.get(position);
        testStep.setStartTime();
    }

    public void setEndTime (int position) {
        TestStep testStep = testSteps.get(position);
        testStep.setEndTime();
    }

    public Date getStartTime (int position) {
        TestStep testStep = testSteps.get(position);
        return testStep.getStartTime();
    }

    public Date getEndTime (int position) {
        TestStep testStep = testSteps.get(position);
        return testStep.getEndTime();
    }

}