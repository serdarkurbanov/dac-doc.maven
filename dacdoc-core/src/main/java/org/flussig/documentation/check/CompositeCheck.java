package org.flussig.documentation.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Checks multiple checks
 */
public class CompositeCheck extends SingleExecutionCheck {
    Collection<Check> checks = new ArrayList<>();

    public Collection<Check> getChecks() {
        return checks;
    }

    public CompositeCheck(Collection<Check> checks) {
        this.checks = checks;
    }

    @Override
    public CheckResult performCheck() {
        Collection<CheckResult> results = checks.stream().map(Check::execute).collect(Collectors.toList());

        return CheckResult.fromMultiple(results);
    }
}
