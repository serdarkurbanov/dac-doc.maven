package org.flussig.documentation;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.flussig.documentation.text.Anchor;
import org.flussig.documentation.text.Reader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Compile goal:
 * searches through README.md files in source directory and subdirectories,
 * replaces placeholders for DACDOC tests with green/red/orange/grey pics
 */
@Mojo(name = "compile")
public class DacDocCompile
    extends AbstractMojo
{
    @Parameter(readonly = true, defaultValue = "${project.build.sourceDirectory}")
    private File srcDirectory;

    public void execute() throws MojoExecutionException
    {
        try {
            File allSourceDir = srcDirectory.getParentFile().getParentFile();

            getLog().info( String.format("Source directory: %s", allSourceDir.getAbsolutePath()));

            // prepare source directory: create resource folder with images for check results (if not exists)
// TODO: uncomment
//            File srcResourceDirectory = new File(getClass().getClassLoader().getResource("circle-green-12px.png").getFile()).getParentFile();
//            getLog().info( String.format("Resource directory: %s", srcResourceDirectory.getAbsolutePath()));
//
//            File destResourceDirectory = Path.of(srcDirectory.getAbsolutePath(), Constants.RESOURCES).toFile().getParentFile();
//            getLog().info( String.format("Dest resource directory: %s", destResourceDirectory.getAbsolutePath()));
//
//            if(!destResourceDirectory.exists()) {
//                destResourceDirectory.mkdir();
//                getLog().info( String.format("Dest resource directory created: %s", destResourceDirectory.getAbsolutePath()));
//
//            }
//
//            File destDacDocResourceDirectory = Path.of(destResourceDirectory.getAbsolutePath(), Constants.DACDOC_RESOURCES).toFile();
//            getLog().info( String.format("DacDoc resource directory: %s", destDacDocResourceDirectory.getAbsolutePath()));
//
//
//            if(!destDacDocResourceDirectory.exists()) {
//                destDacDocResourceDirectory.mkdir();
//                getLog().info( String.format("DacDoc resource directory created: %s", destDacDocResourceDirectory.getAbsolutePath()));
//            }
//
//            // copy all files from source to dest
//            Files.copy(srcResourceDirectory.toPath(), destDacDocResourceDirectory.toPath());
//            getLog().info( String.format("Resources copied from source to dest: %s %s", srcResourceDirectory.getAbsolutePath(), destDacDocResourceDirectory.getAbsolutePath()));


            // collect all readme files
            Set<File> readmeFiles = Reader.findMarkdownFiles(allSourceDir.toPath());

            // parse and find all placeholders
            Map<File, Set<Anchor>> parsedAnchors = Reader.parseFiles(readmeFiles);

            // create map between placeholders and checks
            var checkMap = Reader.createCheckMap(parsedAnchors);

            // replace DACDOC placeholders with indicators of check results
            Map<File, String> processedFiles = Reader.getProcesedReadmeFiles(checkMap, Path.of(allSourceDir.getAbsolutePath(), Constants.DACDOC_RESOURCES));

            // add indicators of check results to each readme file
            for(var fileContent: processedFiles.entrySet()) {

                Files.writeString(fileContent.getKey().toPath(), fileContent.getValue());
            }
        } catch(Exception e) {
            throw new MojoExecutionException("exception while executing dacdoc-maven-plugin compile goal " + e.getMessage());
        }
    }
}
