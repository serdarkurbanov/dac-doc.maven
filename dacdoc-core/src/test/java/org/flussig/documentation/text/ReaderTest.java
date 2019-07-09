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
    public void parseFilesTest() {
        File readme = new File(getClass()
                .getClassLoader()
                .getResource("README_TEST.md")
                .getFile());

        Set<File> files = new HashSet<>();
        files.add(readme);

        try {
            Map<File, Set<Anchor>> parsedAchors = Reader.parseFiles(files);

            assertEquals( 10, parsedAchors.get(readme).size());
        } catch(Exception e) {
            fail("DACDOC is unable to read file " + readme.getPath() + "\n" + e.getMessage());
        }

    }
}