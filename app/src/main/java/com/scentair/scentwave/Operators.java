package com.scentair.scentwave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class Operators {
    private static final String TAG_OPERATORS = "operator_name";

    static public ArrayList<String> operatorList;

    JSONArray json_operators = null;

    // Constructor
    // Read the list of operators from the database on the network
    public Operators(String dbAddress) {
        //Initialize the operator array list
        operatorList = new ArrayList<String>();

        // Get the JSON
        String url = "http://" + dbAddress + "/dbtest.php/operators";

        JSONParser jParser = new JSONParser();

        json_operators = jParser.getJSONFromUrl(url);

        try {
            // looping through all operators
            for (int i = 0; i < json_operators.length(); i++)
            {
                JSONObject q = json_operators.getJSONObject(i);

                // Storing each json item in variables
                operatorList.add(q.getString(TAG_OPERATORS));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
        }
    }

    public String[] getOperators() {
        return operatorList.toArray(new String[operatorList.size()]);
    }
}