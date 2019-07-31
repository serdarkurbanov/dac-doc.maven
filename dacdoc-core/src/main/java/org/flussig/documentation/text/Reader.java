package org.flussig.documentation.text;

import org.flussig.documentation.Constants;
import org.flussig.documentation.check.Check;
import org.flussig.documentation.check.CheckResult;
import org.flussig.documentation.check.CompositeCheck;
import org.flussig.documentation.check.UrlCheck;
import org.flussig.documentation.exception.DacDocException;
import org.flussig.documentation.exception.DacDocParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reader accepts File handlers for given project and extracts all DacDoc anchors (placeholders)
 */
public class Reader {
    private static Pattern anchorPlaceholderPattern = Pattern.compile(String.format(
            "%s%s((.|\\n|\\r)*?)%s",
            Constants.ANCHOR_FRAMING,
            Constants.ANCHOR_KEYWORD,
            Constants.ANCHOR_FRAMING));


    /**
     * Get all markdown files in given directory
     */
    public static Set<File> findMarkdownFiles(Path path) throws DacDocException {
        Set<File> result = new HashSet<>();

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
            Matcher dacdocPlaceholderMatcher = anchorPlaceholderPattern.matcher(content);

            while(dacdocPlaceholderMatcher.find()) {
                String dacdocAnchorFullText = dacdocPlaceholderMatcher.group();

                Anchor anchor = Anchor.from(dacdocAnchorFullText);

                anchors.add(anchor);
            }
        }

        attachChecks(result);

        return result;
    }

    /**
     * loops through anchor-check map and replace anchors with results in files
     */
    public static Map<File, String> getTransformedFiles(Map<File, Set<Anchor>> checkMap, Path dacdocResourceFirectory) throws DacDocParseException {
        Set<File> files = checkMap.keySet();

        // map file and its initial content
        Map<File, String> fileContents = files.stream()
                .collect(Collectors.toMap(f -> f, f -> {
                    try {
                        return Files.readString(f.toPath());
                    } catch(IOException e) {
                        return null;
                    }
                }));

        for(File file: files) {
            String newFileContent = fileContents.get(file);

            // replace each anchor with new content after checks
            for(Anchor anchor: checkMap.get(file)) {
                Check check = anchor.getCheck();

                CheckResult checkResult = check.execute();

                // replace given anchor with test result
                newFileContent = newFileContent.replace(
                        anchor.getFullText(),
                        anchor.getTransformedText(checkResult, dacdocResourceFirectory, file));
            }

            // add aggregate check for the file to the top of the file
            List<Check> fileChecks = checkMap.get(file).stream().map(Anchor::getCheck).collect(Collectors.toList());
            Check aggregateFileCheck = new CompositeCheck(fileChecks);

            String fileCheckImageString =
                    Anchor.getCheckResultImage(aggregateFileCheck.execute(), dacdocResourceFirectory, file, file.getName());

            newFileContent = String.format("%s\n\n%s", fileCheckImageString, newFileContent);

            fileContents.replace(file, newFileContent);
        }

        return fileContents;
    }

    /**
     * Map file-anchor tuple to checks
     */
    private static void attachChecks(Map<File, Set<Anchor>> fileAnchorMap) {
        // convert fileAnchorMap to set of tuples
        Set<FileAnchorTuple> tuples = fileAnchorMap.entrySet().stream()
                .flatMap(kv -> kv.getValue().stream().map(anchor -> new FileAnchorTuple(kv.getKey(), anchor)))
                .collect(Collectors.toSet());

        // first loop through anchors: assign all checks
        fillChecksInitial(tuples);

        // second loop through anchors: put values into composite checks
        fillChecksComposite(tuples);
    }

    // TODO: avoid circular dependencies for composite checks
    private static void fillChecksComposite(Set<FileAnchorTuple> tuples) {
        for(FileAnchorTuple fileAnchorTuple: tuples.stream().filter(t -> t.getAnchor().getAnchorType() == AnchorType.COMPOSITE).collect(Collectors.toSet())) {
            CompositeCheck compositeCheck = (CompositeCheck)fileAnchorTuple.getAnchor().getCheck();

            Collection<String> ids = fileAnchorTuple.getAnchor().getIds();

            // find checks for all ids and attach to composite check
            for(String id: ids) {
                Check subCheck;

                Optional<FileAnchorTuple> subTuple = tuples.stream().filter(t -> t.getAnchor().getId().equals(id)).findFirst();

                if(subTuple.isEmpty()) {
                    subCheck = Check.unknownCheck;
                } else {
                    subCheck = subTuple.get().getAnchor().getCheck();
                }

                compositeCheck.getChecks().add(subCheck);
            }
        }
    }

    private static void fillChecksInitial(Set<FileAnchorTuple> tuples) {
        for(FileAnchorTuple fileAnchorTuple: tuples) {
            Anchor anchor = fileAnchorTuple.getAnchor();
            File file = fileAnchorTuple.getFile();
            anchor.setCheck(generateCheck(anchor, file));
        }
    }

    private static Check generateCheck(Anchor anchor, File file) {
        Check check;
        if(anchor.getAnchorType() == AnchorType.COMPOSITE) {
            // for composite type: put empty composite check
            check = new CompositeCheck(new ArrayList());
        } else {
            // for primitive type: define type of check and add it
            if(anchor.getTestId().equals(Constants.DEFAULT_TEST_ID)) {
                check = new UrlCheck(anchor.getArgument(), file);
            } else {
                check = Check.unknownCheck;
            }
        }
        return check;
    }

    private static class FileAnchorTuple {
        private File file;
        private Anchor anchor;

        public FileAnchorTuple(File file, Anchor anchor) {
            this.anchor = anchor;
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public Anchor getAnchor() {
            return anchor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileAnchorTuple that = (FileAnchorTuple) o;
            return Objects.equals(file, that.file) &&
                    Objects.equals(anchor, that.anchor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, anchor);
        }
    }
}
