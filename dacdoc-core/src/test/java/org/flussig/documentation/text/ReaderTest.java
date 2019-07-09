package org.flussig.documentation.text;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReaderTest {

    @Test
    public void findFilesTest() {
        File readmeRoot = new File(getClass()
                .getClassLoader()
                .getResource("README_TEST.md")
                .getFile())
                .getParentFile();

        try {
            Set<File> readmeFiles = Reader.findMarkupFiles(readmeRoot.toPath());

            assertEquals(readmeFiles.size(), 1);
        } catch(Exception e) {
            fail("DACDOC is unable to list markup files in folder " + readmeRoot.getAbsolutePath());
        }
    }

    @Test
    public void parseFilesTest() {
        File readme = new File(getClass()
                .getClassLoader()
                .getResource("README_TEST.md")
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
}