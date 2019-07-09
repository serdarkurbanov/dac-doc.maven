package org.flussig.documentation.check;

/**
 * Check that executes only once and then stores result to avoid executing multiple times
 */
public abstract class SingleExecutionCheck implements Check {
    private volatile boolean executed = false;
    private CheckResult result;

    @Override
    public CheckResult execute() {
        if(!executed) {
            init();
        }

        return result;
    }

    /**
     * Initialize value of the result
     */
    private synchronized void init() {
        if(executed)
            return;

        result = performCheck();
    }

    /**
     * actual method to perform result
     */
    abstract CheckResult performCheck();
}
