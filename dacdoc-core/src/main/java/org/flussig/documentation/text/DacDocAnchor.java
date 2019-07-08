package org.flussig.documentation.text;

import org.flussig.documentation.Constants;
import org.flussig.documentation.exception.DacDocException;
import org.flussig.documentation.util.Strings;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class contains information of DACDOC placeholder and associated tags
 */
public abstract class DacDocAnchor {
    protected String fullText;
    protected String id;

    /**
     * Generate anchor instance from full string
     */
    public static DacDocAnchor from(String fullText) throws DacDocException {
        DacDocAnchor result = null;

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

        if(!(fullTextStripFraming.startsWith("{") || fullTextStripFraming.startsWith("{")) ||
                (contentParameterTuple.content == null && contentParameterTuple.paremeters == null)) {
            throw new DacDocException(
                    String.format(
                            "expected format for DACDOC placeholder: %s or %s",
                            String.format("%s%s{...}(...)%s", Constants.ANCHOR_FRAMING, Constants.ANCHOR_KEYWORD, Constants.ANCHOR_FRAMING),
                            String.format("%s%s(...)%s", Constants.ANCHOR_FRAMING, Constants.ANCHOR_KEYWORD, Constants.ANCHOR_FRAMING)));
        }

        // parameters are present
        Map<String, String> paramMap = extractParameterMap(contentParameterTuple);

        // content present -> primitive anchor type; if not -> complex type
        if(Strings.isNullOrEmpty(contentParameterTuple.content)) {
            result = getComplexAnchor(paramMap);
        } else {
            result = getPrimitiveAnchor(contentParameterTuple.content, paramMap);

        }

        // extract or generate ID
        result.fullText = fullText;
        attachID(result, paramMap);

        return result;
    }

    private static DacDocAnchor getPrimitiveAnchor(String content, Map<String, String> paramMap) {
        DacDocAnchor result;
        String testId;

        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_TEST_ID)) {
            testId = paramMap.get(Constants.ANCHOR_PARAMETER_TEST_ID);
        } else {
            testId = Constants.DEFAULT_TEST_ID;
        }

        result = new DacDocPrimitiveAnchor(content, testId);
        return result;
    }

    private static DacDocAnchor getComplexAnchor(Map<String, String> paramMap) {
        DacDocAnchor result;
        Collection<String> ids = new ArrayList<>();
        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_IDS)) {
            ids = Arrays.stream(paramMap.get(Constants.ANCHOR_PARAMETER_IDS).split(Constants.ANCHOR_PARAMETER_IDS_SEPARATOR))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        result = new DacDocComplexAnchor(ids);
        return result;
    }

    private static void attachID(DacDocAnchor result, Map<String, String> paramMap) {
        if(paramMap != null && !paramMap.isEmpty() && paramMap.containsKey(Constants.ANCHOR_PARAMETER_ID)) {
            result.id =  paramMap.get(Constants.ANCHOR_PARAMETER_ID);
        } else {
            result.id = UUID.randomUUID().toString();
        }
    }

    private static Map<String, String> extractParameterMap(ContentParameterTuple contentParameterTuple) {
        Map<String, String> paramMap = null;
        if(!Strings.isNullOrEmpty(contentParameterTuple.paremeters)) {
            paramMap = Arrays.stream(contentParameterTuple.paremeters.split(Constants.ANCHOR_PARAMETER_SEPARATOR))
                    .map(p -> p.split(Constants.ANCHOR_PARAMETER_KEY_VALUE_SEPARATOR))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        }
        return paramMap;
    }

    /**
     * Check anchor for internal consistency
     */
    public abstract DacDocValidationResult validate();

    /**
     * Tuple of content string and parameter string of a anchor
     * */
    private static class ContentParameterTuple {
        String content;
        String paremeters;

        // after stripping text is left with {...}(...) or (...)
        public ContentParameterTuple(String fullTextStripFraming) {
            if(fullTextStripFraming.startsWith("(")) {
                Matcher parenthesesMatcher = Pattern.compile("\\(([^)]*)\\)").matcher(fullTextStripFraming);
                if (parenthesesMatcher.find()) {
                    paremeters = parenthesesMatcher.group(1);
                }
            }

            Matcher curlyBracketMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(fullTextStripFraming);
            if (curlyBracketMatcher.find()) {
                content = curlyBracketMatcher.group(1);
            }
        }
    }
}
