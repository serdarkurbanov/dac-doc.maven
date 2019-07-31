package org.flussig.documentation.check;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents check that needs to be executed
 */
public abstract class Check {
    protected String argument;
    protected File file;

    public Check(String argument, File file) {
        this.file = file;
        this.argument = argument;
    }

    public abstract CheckResult execute();

    /**
     * Default check that is executed if no other check matches
     */
    public static Check unknownCheck = new Check(null, null) {
        @Override
        public CheckResult execute() {
            return new CheckResult("",LocalDateTime.now(), CheckStatus.GREY);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Check check = (Check) o;
        return Objects.equals(argument, check.argument) &&
                Objects.equals(file, check.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument, file);
    }
}
