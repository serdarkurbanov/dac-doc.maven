package org.flussig.documentation.check;

import java.util.Collection;

/**
 * Status of the check
 * GREY: check not found or failed to execute
 * GREEN: check passed successfully
 * ORANGE: some tests a composite check have passed successfully, some are not
 * RED: check failed
 */
public enum CheckStatus {
    GREY, GREEN, ORANGE, RED;

    public static CheckStatus fromMultiple(Collection<CheckStatus> testResults) {
        if(testResults == null || testResults.isEmpty()) {
            return GREY;
        }

        if(testResults.stream().allMatch(result -> result.equals(GREEN))) {
            return GREEN;
        } else if(testResults.stream().allMatch(result -> result.equals(GREY))) {
            return GREY;
        } else if(testResults.stream().allMatch(result -> result.equals(RED) || result.equals(GREY))) {
            return RED;
        } else {
            return ORANGE;
        }
    }
}
