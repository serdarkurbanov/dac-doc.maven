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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Compile goal:
 * searches through README.md files in source directory and subdirectories,
 * replaces placeholders for DACDOC tests with green/red/orange/grey pics
 */
@Mojo(name = "compile")
public class DacDocCompile
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    public void execute() throws MojoExecutionException
    {
        try {
            // prepare source directory: create resource folder with images for check results (if not exists)

            // collect all readme files

            // collect all DACDOC placeholders within readme files

            // create map of DACDOC placeholders in readme files to DACDOC checks

            // validate for consistency of DACDOC checks

            // perform all DACDOC checks

            // replace DACDOC placeholders with indicators of check results

            // add indicators of check results to each readme file
        } catch(Exception e) {
            throw new MojoExecutionException("exception while executing dacdoc-maven-plugin compile goal", e);
        }

        getLog().info( "Hello, world." );

        File f = outputDirectory;

        if ( !f.exists() )
        {
            f.mkdirs();
        }

        File touch = new File( f, "touch.txt" );

        FileWriter w = null;
        try
        {
            w = new FileWriter( touch );

            w.write( "touch.txt" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + touch, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
}
