package com.scentair.scentwave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// This class builds a list of failures and loads them from the test database
public class Failures extends ArrayList<Failures> {
    private static final String TAG_FAILURE_ID = "failureId";
    private static final String TAG_FAILURE_TEXT = "failureText";

    public ArrayList<Failure> failures;

    JSONArray json_operators = null;

    //alternate constructor to copy main values
    public Failures (int dummy){
        // do nothing but reserve memory
    }

    // Constructor
    public Failures (String dbAddress) {
       // Read the array of tests from the database on the network

        //Initialize the operator array list
        failures = new ArrayList<Failure>();

        // Get the JSON
        String url = "http://" + dbAddress + "/dbtest.php/failures";

        JSONParser jParser = new JSONParser();

        json_operators = jParser.getJSONFromUrl(url);

        try {

            // looping through all quakes
            for (int i = 0; i < json_operators.length(); i++)
            {
                Failure failure= new Failure();
                JSONObject q = json_operators.getJSONObject(i);

                // Storing each json item in variables
                failure.failureId = q.getInt(TAG_FAILURE_ID);
                failure.failureText = q.getString(TAG_FAILURE_TEXT);

                failures.add(failure);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
        }
    }

    public Integer getMaxFailures () {
        return failures.size();
    }

    public Integer getFailureId (int position) {
        Failure failure = failures.get(position);
        return failure.failureId;
    }

    public String getFailureText (int position) {
        Failure failure = failures.get(position);
        return failure.failureText;
    }

    public ArrayList<Failure> getFailures() {
        ArrayList<Failure> tempFailures = failures;
        return tempFailures;
    }
}