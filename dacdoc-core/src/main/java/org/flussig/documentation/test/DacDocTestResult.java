package org.flussig.documentation.test;

import java.util.Collection;

/**
 * Result of the test
 * GREY: test not found or failed to execute
 * GREEN: test passed successfully
 * ORANGE: some tests a composite test have passed successfully, some are not
 * RED: test failed
 */
public enum DacDocTestResult {
    GREY, GREEN, ORANGE, RED;

    public DacDocTestResult fromMultiple(Collection<DacDocTestResult> testResults) {
        if(testResults == null || testResults.isEmpty()) {
            return GREY;
        }

        if(testResults.stream().allMatch(result -> result.equals(GREEN))) {
            return GREEN;
        } else if(testResults.stream().allMatch(result -> result.equals(RED))) {
            return RED;
        } else if(testResults.stream().allMatch(result -> result.equals(GREY))) {
            return GREY;
        } else {
            return ORANGE;
        }
    }
}
