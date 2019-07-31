package org.flussig.documentation.check;

import org.flussig.documentation.exception.DacDocParseException;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests relative link
 */
public class UrlCheck extends SingleExecutionCheck {
    private static final int REQUEST_TIMEOUT_MS = 500;
    private static final String REQUEST_METHOD = "GET";

    private static Pattern mdUrlPattern = Pattern.compile(String.format("\\[(.*?)\\]\\((.*?)\\)"));

    /**
     * extracting uri from markdown format for link
     * options:
     * direct uri: google.com
     * []() syntax: [mylink](google.com)
     * TODO: not supported now - [] syntax: [mylink] .... [mylink] = google.com
     */
    private static String extractMarkdownUri(String argument) throws DacDocParseException {
        Matcher matcher = mdUrlPattern.matcher(argument);

        // [...](...)
        if(matcher.matches()) {
            return matcher.group(2);
        } else {
            return argument;
        }
    }

    public UrlCheck(String argument, File file) {
        super(argument, file);
    }

    @Override
    public CheckResult performCheck() {
        try {
            String uri = extractMarkdownUri(argument);

            URI parsedUri = URI.create(uri);

            if(parsedUri.isAbsolute()) {
                return executeAbsolutePath(uri);
            } else {
                return executeRelativePath(uri);
            }
        } catch(Exception e) {
            return new CheckResult(e.getMessage(), LocalDateTime.now(), CheckStatus.RED);
        }
    }

    private CheckResult executeRelativePath(String uri) {
        try {
            Path testPath = Path.of(file.getParentFile().getPath(), uri);

            File testFile = new File(testPath.toUri());

            return testFile.exists() ?
                    new CheckResult("", LocalDateTime.now(), CheckStatus.GREEN) :
                    new CheckResult("", LocalDateTime.now(), CheckStatus.RED);
        } catch(Exception e) {
            return new CheckResult(e.getMessage(), LocalDateTime.now(), CheckStatus.RED);
        }
    }

    private CheckResult executeAbsolutePath(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(REQUEST_METHOD);
            con.setConnectTimeout(REQUEST_TIMEOUT_MS);
            con.setReadTimeout(REQUEST_TIMEOUT_MS);
            int responseCode = con.getResponseCode();

            if(responseCode > 299) {
                return new CheckResult("", LocalDateTime.now(), CheckStatus.RED);
            } else {
                return new CheckResult("", LocalDateTime.now(), CheckStatus.GREEN);
            }
        } catch(Exception e) {
            return new CheckResult(e.getMessage(), LocalDateTime.now(), CheckStatus.RED);
        }
    }
}
