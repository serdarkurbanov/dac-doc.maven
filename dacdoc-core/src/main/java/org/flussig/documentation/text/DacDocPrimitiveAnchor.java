package org.flussig.documentation.text;

/**
 * Class contains information of DACDOC primitive placeholder
 * Primitive placeholder contains reference to a particular test
 */
public class DacDocPrimitiveAnchor extends DacDocAnchor {
    private String argument;
    private String testId;

    public DacDocPrimitiveAnchor(String argument, String testId) {
        this.argument = argument;
        this.testId = testId;
    }

    @Override
    public DacDocValidationResult validate() {
        return new DacDocValidationResult();
    }
}
