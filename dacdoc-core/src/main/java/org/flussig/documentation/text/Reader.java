package org.flussig.documentation.text;

import org.flussig.documentation.Constants;
import org.flussig.documentation.exception.DacDocException;
import org.flussig.documentation.exception.DacDocParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reader accepts File handlers for given project and extracts all DacDoc anchors (placeholders)
 */
public class Reader {
    /**
     * Get all markup files in given directory
     */
    public static Collection<File> getMarkupFiles(Path path) throws DacDocException {
        List<File> result = new ArrayList<>();

        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".md"))
                    .forEach(result::add);
        } catch(Exception e) {
            throw new DacDocException(
                    String.format(
                            "traversing root folder %s throws exception", path), e);
        }

        return result;
    }

    /**
     * Parse files and extract anchors
     */
    public static Map<File, Set<Anchor>> parseFiles(Set<File> files) throws IOException, DacDocParseException {
        Map<File, Set<Anchor>> result = new HashMap<>();

        for(File f: files) {
            Set<Anchor> anchors = new HashSet<>();
            result.put(f, anchors);

            String content = Files.readString(f.toPath());

            // extract all DACDOC placeholders
            Matcher dacdocPlaceholderMatcher = Pattern.compile(String.format(
                    "%s%s((.|\\n|\\r)*?)%s",
                    Constants.ANCHOR_FRAMING,
                    Constants.ANCHOR_KEYWORD,
                    Constants.ANCHOR_FRAMING)).matcher(content);

            while(dacdocPlaceholderMatcher.find()) {
                String dacdocAnchorFullText = dacdocPlaceholderMatcher.group();

                Anchor anchor = Anchor.from(dacdocAnchorFullText);

                anchors.add(anchor);
            }
        }

        return result;
    }
}
