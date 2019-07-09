package org.flussig.documentation.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Checks multiple checks
 */
public class CompositeCheck implements Check {
    Collection<Check> checks = new ArrayList<>();

    public CompositeCheck(Collection<Check> checks) {
        this.checks = checks;
    }

    @Override
    public CheckResult execute() {
        Collection<CheckResult> results = checks.stream().map(Check::execute).collect(Collectors.toList());

        return CheckResult.fromMultiple(results);
    }
}
