package org.flussig.documentation.text;

import org.flussig.documentation.Constants;
import org.flussig.documentation.check.Check;
import org.flussig.documentation.check.CheckResult;
import org.flussig.documentation.exception.DacDocParseException;
import org.flussig.documentation.util.Strings;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class contains information of DACDOC placeholder and associated tags
 */
public final class Anchor {
    protected String fullText;
    protected String id;
    private Collection<String> ids = new ArrayList<>();
    private String argument;
    private String testId;

    /**
     * Generate anchor instance from full string
     */
    public static Anchor from(String fullText) throws DacDocParseException {
        // format of full text of anchor is !DACDOC{...}(...)! for primitive type or !DACDOC(...)! for composite type
        String fullTextStripFraming = fullText.replaceAll(
                String.format(
                        "^%s%s|%s$",
                        Constants.ANCHOR_FRAMING,
                        Constants.ANCHOR_KEYWORD,
                        Constants.ANCHOR_FRAMING),
                "");

        // extract content from {...} and parameters from (...)
        ContentParameterTuple contentParameterTuple = new ContentParameterTuple(fullTextStripFraming);

        // parameters are present
        Map<String, String> paramMap = extractParameterMap(contentParameterTuple);

        // content present -> primitive anchor type; if not -> complex type
        Anchor result = getAnchor(contentParameterTuple.content, paramMap);

        result.fullText = fullText;

        return result;
    }

    private static Anchor getAnchor(String content, Map<String, String> paramMap) {
        Anchor result = new Anchor();

        // attach argument
        result.argument = content;

        // attach id of the check
        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_TEST_ID)) {
            result.testId = paramMap.get(Constants.ANCHOR_PARAMETER_TEST_ID);
        } else {
            result.testId = Constants.DEFAULT_TEST_ID;
        }

        // attach ids of child anchors
        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_IDS)) {
            result.ids = Arrays.stream(paramMap.get(Constants.ANCHOR_PARAMETER_IDS).split(Constants.ANCHOR_PARAMETER_IDS_SEPARATOR))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        // extract or generate ID
        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_ID)) {
            result.id =  paramMap.get(Constants.ANCHOR_PARAMETER_ID);
        } else {
            result.id = UUID.randomUUID().toString();
        }

        return result;
    }

    private static Map<String, String> extractParameterMap(ContentParameterTuple contentParameterTuple) {
        Map<String, String> paramMap = null;
        if(!Strings.isNullOrEmpty(contentParameterTuple.paremeters)) {
            paramMap = Arrays.stream(contentParameterTuple.paremeters.split(Constants.ANCHOR_PARAMETER_SEPARATOR))
                    .map(String::trim)
                    .map(p -> p.split(Constants.ANCHOR_PARAMETER_KEY_VALUE_SEPARATOR))
                    .collect(Collectors.toMap(kv -> kv[0].trim(), kv -> kv[1].trim()));
        }
        return paramMap;
    }

    private static String getResultImagePath(CheckResult checkResult, Path dacdocResourceDirectory, File currentFile) {
        String imageFileName;
        switch(checkResult) {
            case RED:
                imageFileName = Constants.RED_IND;
                break;
            case GREEN:
                imageFileName = Constants.GREEN_IND;
                break;
            case ORANGE:
                imageFileName = Constants.ORANGE_IND;
                break;
            case GREY:
            default:
                imageFileName = Constants.GREY_IND;
                break;
        }

        return currentFile.getParentFile().toPath().relativize(Path.of(dacdocResourceDirectory.toString(), imageFileName)).toString();
    }

    public static String getCheckResultImage(CheckResult testResult, Path dacdocResourceDirectory, File currentFile, String alt, String comment) {
        return String.format(
                "![%s](%s \"%s\")",
                alt,
                getResultImagePath(testResult, dacdocResourceDirectory, currentFile),
                comment);
    }


    /**
     * Check anchor for internal consistency
     */
    public ValidationResult validate() {
        return new ValidationResult();
    }

    /**
     * Prepares text of the anchor for replacement
     * When check result is acquired, this method will return full text of anchor with DACDOC placeholder stripped away and decorations for showing check results added
     * !DACDOC{xxx}(...)! --> xxx ![test-id](./dacdoc-resources/circle-green-12px.png "comment")
     */
    public String getFullText(CheckResult testResult, Path dacdocResourceDirectory, File currentFile) {
        String resultImage = getCheckResultImage(
                testResult,
                dacdocResourceDirectory,
                currentFile, id,
                String.format("checked on %s", LocalDateTime.now().toString()));

        // if content is not empty, put content and then image reference
        if(Strings.isNullOrEmpty(argument)) {
            return resultImage;
        } else {
            return String.format("%s %s", resultImage, argument);
        }
    }

    public String getFullText() {
        return fullText;
    }

    public String getId() {
        return id;
    }

    public Collection<String> getIds() {
        return ids;
    }

    public String getArgument() {
        return argument;
    }

    public String getTestId() {
        return testId;
    }

    public AnchorType getAnchorType() {
        if(ids.isEmpty()) {
            return AnchorType.PRIMITIVE;
        } else {
            return AnchorType.COMPOSITE;
        }
    }

    private Anchor() {}

    /**
     * Identity of anchor is defined by its full text
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anchor that = (Anchor) o;
        return Objects.equals(fullText, that.fullText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullText);
    }

    @Override
    public String toString() {
        return fullText;
    }

    /**
     * Tuple of content string and parameter string of a anchor
     * */
    private static class ContentParameterTuple {
        private static Pattern argumentPattern = Pattern.compile("\\{([^}]+)\\}");
        private static Pattern parameterPattern = Pattern.compile("\\(([^)]*)\\)");
        private static Pattern fullPatern = Pattern.compile("\\{([^}]+)\\}\\(([^)]*)\\)");

        String content;
        String paremeters;

        // after stripping text is left with {...}(...) or (...) or {...}
        public ContentParameterTuple(String fullTextStripFraming) throws DacDocParseException {
            Matcher fullMatcher = fullPatern.matcher(fullTextStripFraming);
            Matcher argumentMatcher = argumentPattern.matcher(fullTextStripFraming);
            Matcher parameterMatcher = parameterPattern.matcher(fullTextStripFraming);

            if(fullMatcher.matches()) {
                content = fullMatcher.group(1);
                paremeters = fullMatcher.group(2);
            } else if (argumentMatcher.matches()) {
                content = argumentMatcher.group(1);
            } else if (parameterMatcher.matches()) {
                paremeters = parameterMatcher.group(1);
            } else {
                throw new DacDocParseException(
                        String.format(
                                "expected format for DACDOC placeholder parameters: %s or %s or %s. Given string: %s",
                                String.format("%s%s{...}(...)%s", Constants.ANCHOR_FRAMING, Constants.ANCHOR_KEYWORD, Constants.ANCHOR_FRAMING),
                                String.format("%s%s(...)%s", Constants.ANCHOR_FRAMING, Constants.ANCHOR_KEYWORD, Constants.ANCHOR_FRAMING),
                                String.format("%s%s{...}%s", Constants.ANCHOR_FRAMING, Constants.ANCHOR_KEYWORD, Constants.ANCHOR_FRAMING),
                                fullTextStripFraming));
            }
        }
    }
}
