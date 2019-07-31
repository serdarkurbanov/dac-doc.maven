package org.flussig.documentation.check;

import java.time.LocalDateTime;

/**
 * Represents check that needs to be executed
 */
public interface Check {
    CheckResult execute();

    static Check unknownCheck = () ->  new CheckResult("", LocalDateTime.now(), CheckStatus.GREY);
}
