package com.scentair.scentwave;

import com.google.gson.annotations.Expose;

// This class is the tool to link the units, to the tests, to the test runs.
// It is a class because that makes it easier to process into the database.

public class DataLinkRecord {
    @Expose public Integer testruns_id;
    @Expose public Integer unittests_id;
    @Expose public Integer sw1004units_id;

    public DataLinkRecord ( Integer testRunId,Integer UnitTestId,Integer UnitId) {
        this.sw1004units_id = UnitId;
        this.testruns_id = testRunId;
        this.unittests_id = UnitTestId;
    }
}