package org.flussig.documentation.text;

import org.flussig.documentation.Constants;
import org.flussig.documentation.exception.DacDocParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reader accepts File handlers for given project and extracts all DacDoc anchors (placeholders)
 */
public class DacDocReader {
    /**
     * Parse files and extract anchors
     */
    public static Map<File, Set<DacDocAnchor>> parseFiles(Set<File> files) throws IOException, DacDocParseException {
        Map<File, Set<DacDocAnchor>> result = new HashMap<>();

        for(File f: files) {
            Set<DacDocAnchor> anchors = new HashSet<>();
            result.put(f, anchors);

            String content = Files.readString(f.toPath());

            // extract all DACDOC placeholders
            Matcher dacdocPlaceholderMatcher = Pattern.compile(String.format(
                    "^%s%s|%s$",
                    Constants.ANCHOR_FRAMING,
                    Constants.ANCHOR_KEYWORD,
                    Constants.ANCHOR_FRAMING)).matcher(content);

            while(dacdocPlaceholderMatcher.find()) {
                String dacdocAnchorFullText = dacdocPlaceholderMatcher.group();

                DacDocAnchor dacDocAnchor = DacDocAnchor.from(dacdocAnchorFullText);

                anchors.add(dacDocAnchor);
            }
        }

        return result;
    }
}
