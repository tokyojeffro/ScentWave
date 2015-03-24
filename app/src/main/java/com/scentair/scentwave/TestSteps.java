package com.scentair.scentwave;

import java.util.ArrayList;
import java.util.Collections;

public class TestSteps {

    public static final TestStep testStep1 = new TestStep(
            1,
            "Plug in power",
            "",
            "Note change in state and current");

    public static final TestStep testStep2 = new TestStep(
            2,
            "Scan Mitec Barcode",
            "Scan Scentair Barcode",
            "Barcodes are entered");

    public static final TestStep testStep3 = new TestStep(
            1,
            "Verify Backlight Off",
            "",
            "Check backlight off after 2 mins");

    public static final TestStep testStep4 = new TestStep(
            2,
            "Step 4 check 1",
            "Step 4 check 2",
            "Check step 4");

    public ArrayList<TestStep> testSteps = new ArrayList<TestStep>(4);

    public TestSteps () {
        testSteps.add(testStep1);
        testSteps.add(testStep2);
        testSteps.add(testStep3);
        testSteps.add(testStep4);
        }

    public ArrayList<TestStep> getTestSteps() {
        return testSteps;
    }
    public TestStep getTestStep (Integer testStepNum) {
        return testSteps.get(testStepNum);
    }
}