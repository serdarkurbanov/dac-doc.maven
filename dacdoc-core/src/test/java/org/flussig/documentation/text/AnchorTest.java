package org.flussig.documentation.text;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit check for simple Anchor.
 */
public class AnchorTest {
    @Test
    public void testParseDacDocPlaceholder() {
        String anchorText = "!DACDOC{[self](./README.md)}!";

        try {
            Anchor anchor = Anchor.from(anchorText);

            ValidationResult validationResult = anchor.validate();

            assertTrue("internal validation of dacdoc anchor failed", validationResult.getIssues().isEmpty() );
        } catch(Exception e) {
            fail("exception when creating anchor");
        }
    }
}
