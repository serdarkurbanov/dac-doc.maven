package org.flussig.documentation.text;

import org.flussig.documentation.check.Check;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ReaderTest {

    @Test
    public void findFilesTest() {
        File readmeRoot = new File(getClass()
                .getClassLoader()
                .getResource("README.md")
                .getFile())
                .getParentFile();

        try {
            Set<File> readmeFiles = Reader.findMarkdownFiles(readmeRoot.toPath());

            assertEquals(readmeFiles.size(), 1);
        } catch(Exception e) {
            fail("DACDOC is unable to list markdown files in folder " + readmeRoot.getAbsolutePath());
        }
    }

    @Test
    public void parseFilesTest() {
        File readme = new File(getClass()
                .getClassLoader()
                .getResource("README.md")
                .getFile());

        Set<File> files = new HashSet<>();
        files.add(readme);

        try {
            Map<File, Set<Anchor>> parsedAnchors = Reader.parseFiles(files);

            assertEquals( 10, parsedAnchors.get(readme).size());
        } catch(Exception e) {
            fail("DACDOC is unable to read file " + readme.getPath() + "\n" + e.getMessage());
        }
    }

    @Test
    public void createCheckMap() {
        File readme = new File(getClass()
                .getClassLoader()
                .getResource("README.md")
                .getFile());

        Set<File> files = new HashSet<>();
        files.add(readme);

        try {
            Map<File, Set<Anchor>> parsedAnchors = Reader.parseFiles(files);

            Set<Check> checks = parsedAnchors.get(readme).stream().map(Anchor::getCheck).collect(Collectors.toSet());

            assertEquals(10, parsedAnchors.get(readme).size());
            assertEquals(5, checks.size());
            assertTrue(checks.stream().allMatch(Objects::nonNull));
        } catch(Exception e) {
            fail("DACDOC is unable map anchors to checks " + readme.getPath() + "\n" + e.getMessage());
        }
    }

    @Test
    public void getProcessedReadmeFiles() {
        File readme = new File(getClass()
                .getClassLoader()
                .getResource("README.md")
                .getFile());

        Path parentDir = Path.of(readme.getParentFile().getParentFile().toString(), "classes");

        Set<File> files = new HashSet<>();
        files.add(readme);

        try {
            Map<File, Set<Anchor>> parsedAnchors = Reader.parseFiles(files);

            Map<File, String> processedFiles = Reader.getTransformedFiles(parsedAnchors, parentDir);

            assertEquals(1, processedFiles.size());
        } catch(Exception e) {
            fail("DACDOC is unable map anchors to checks " + readme.getPath() + "\n" + e.getMessage());
        }
    }
}