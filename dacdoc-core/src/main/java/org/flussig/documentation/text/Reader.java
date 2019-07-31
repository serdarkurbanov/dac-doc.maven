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
import java.time.LocalDateTime;
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

        return result;
    }

    /**
     * Map file-anchor tuple to checks
     */
    public static Map<FileAnchorTuple, Check> createCheckMap(Map<File, Set<Anchor>> fileAnchorMap) throws DacDocParseException {
        // convert fileAnchorMap to set of tuples
        Set<FileAnchorTuple> tuples = fileAnchorMap.entrySet().stream()
                .flatMap(kv -> kv.getValue().stream().map(anchor -> new FileAnchorTuple(kv.getKey(), anchor)))
                .collect(Collectors.toSet());

        // assign each tuple a check
        Map<FileAnchorTuple, Check> result = new HashMap<>();

        // first loop through anchors: assign all checks
        fillChecksInitial(tuples, result);

        // second loop through anchors: put values into composite checks
        fillChecksComposite(tuples, result);

        return result;
    }

    /**
     * loops through anchor-check map and replace anchors with results in files
     */
    public static Map<File, String> getProcessedReadmeFiles(Map<FileAnchorTuple, Check> checkMap, Path dacdocResourceFirectory) throws DacDocParseException {
        // map file and its initial content
        Map<File, String> processedFiles = checkMap.keySet().stream()
                .map(FileAnchorTuple::getFile)
                .distinct()
                .collect(Collectors.toMap(f -> f, f -> {
                    try {
                        return Files.readString(f.toPath());
                    } catch(IOException e) {
                        return null;
                    }
                }));

        // map file and its list of checks
        Map<File, List<Check>> processedFilesChecks = checkMap.entrySet().stream()
                .collect(Collectors.groupingBy(fileCheck -> fileCheck.getKey().getFile()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        kv -> kv.getValue().stream().map(Map.Entry::getValue).collect(Collectors.toList())));

        // create map of files and file-level composite check
        Map<File, Check> fileToCompositeCheckMap = processedFilesChecks.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        kv -> new CompositeCheck(kv.getValue())));

        // replace anchors with new content
        for(var anchorCheck: checkMap.entrySet()) {
            Anchor anchor = anchorCheck.getKey().getAnchor();
            File file = anchorCheck.getKey().getFile();
            Check check = anchorCheck.getValue();

            CheckResult checkResult = check.execute();

            // replace given anchor with test result
            String newFileContent = processedFiles
                    .get(file)
                    .replace(anchor.getFullText(), anchor.getFullText(checkResult.getStatus(), dacdocResourceFirectory, file));

            processedFiles.replace(file, newFileContent);
        }

        // create composite checks for each file and put it in the beginning of the file
        for(var fileCheck: fileToCompositeCheckMap.entrySet()) {
            File file = fileCheck.getKey();
            Check check = fileCheck.getValue();

            String oldFileContent = processedFiles.get(file);

            String fileCheckImageString = Anchor.getCheckResultImage(
                    check.execute().getStatus(),
                    dacdocResourceFirectory,
                    file,
                    file.getName(),
                    String.format("checked on %s", LocalDateTime.now().toString()));

            String newFileContent = String.format("%s\n\n%s", fileCheckImageString, oldFileContent);

            processedFiles.replace(file, newFileContent);
        }

        return processedFiles;
    }

    // TODO: avoid circular dependencies for composite checks
    private static void fillChecksComposite(Set<FileAnchorTuple> tuples, Map<FileAnchorTuple, Check> result) {
        for(FileAnchorTuple fileAnchorTuple: tuples.stream().filter(t -> t.getAnchor().getAnchorType() == AnchorType.COMPOSITE).collect(Collectors.toSet())) {
            CompositeCheck compositeCheck = (CompositeCheck)result.get(fileAnchorTuple);

            Collection<String> ids = fileAnchorTuple.getAnchor().getIds();

            // find checks for all ids and attach to composite check
            for(String id: ids) {
                Check subCheck;

                Optional<FileAnchorTuple> subTuple = tuples.stream().filter(t -> t.getAnchor().getId().equals(id)).findFirst();

                if(subTuple.isEmpty()) {
                    subCheck = Check.unknownCheck;
                } else {
                    subCheck = result.get(subTuple.get());
                }

                compositeCheck.getChecks().add(subCheck);
            }
        }
    }

    private static void fillChecksInitial(Set<FileAnchorTuple> tuples, Map<FileAnchorTuple, Check> result) throws DacDocParseException {
        Set<Check> checks = new HashSet<>();

        for(FileAnchorTuple fileAnchorTuple: tuples) {
            Check check;
            Anchor anchor = fileAnchorTuple.getAnchor();
            File file = fileAnchorTuple.getFile();

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

            if(!checks.contains(check)) {
                checks.add(check);
            }

            result.put(fileAnchorTuple, check);
        }
    }

    public static class FileAnchorTuple {
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
