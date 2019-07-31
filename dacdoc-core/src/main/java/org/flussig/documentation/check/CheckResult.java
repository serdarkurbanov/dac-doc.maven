package org.flussig.documentation.check;

import java.time.LocalDateTime;

/**
 * Full information on the result of the test: message, status etc
 */
public class CheckResult {
    private String message;
    private LocalDateTime time;
    private CheckStatus status;

    public CheckResult(String message, LocalDateTime time, CheckStatus status) {
        this.message = message;
        this.time = time;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public CheckStatus getStatus() {
        return status;
    }
}
