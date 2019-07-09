package org.flussig.documentation.check;

/**
 * Default check that is assigned if other tests are not found
 */
public class UnknownCheck implements Check {
    @Override
    public CheckResult execute() {
        return CheckResult.GREY;
    }
}
