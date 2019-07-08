package org.flussig.documentation.text;

import java.util.Collection;

/**
 * Class contains information of DACDOC complex placeholder
 * Complex placeholder contains references to other placeholders
 */
public class DacDocComplexAnchor extends DacDocAnchor {
    private Collection<String> ids;

    public DacDocComplexAnchor(Collection<String> ids) {
        this.ids = ids;
    }

    @Override
    public DacDocValidationResult validate() {
        return new DacDocValidationResult();
    }
}
