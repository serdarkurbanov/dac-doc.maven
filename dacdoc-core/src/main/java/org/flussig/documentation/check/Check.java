package org.flussig.documentation.check;

/**
 * Represents check that needs to be executed
 */
public interface Check {
    CheckResult execute();

    static Check unknownCheck = () -> CheckResult.GREY;
}
