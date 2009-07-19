package org.codehaus.mojo.unix.maven.plugin;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.maven.plugin.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.plexus.util.*;

import java.io.*;

/**
 * Executes "dpkg-scanpackages".
 * <p/>
 * By default the entire local repository will be scanned.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal dpkg-scanpackages
 */
public class DpkgScanPackagesMojo
    extends AbstractMojo
{
    /**
     * The base of where to scan. If not set, it will default to the entire local repository.
     *
     * @parameter expression="${maven.unix.dpkg-scanpackages.root}"
     */
    private File root;

    /**
     * @parameter expression="${maven.unix.dpkg-scanpackages.prefix}"
     */
    private String pathPrefix;

    /**
     * @parameter expression="${maven.unix.dpkg-scanpackages.override}" default-value="/dev/null"
     */
    private String overrideFile;

    /**
     * @parameter expression="${maven.unix.dpkg-scanpackages.multiversion}" default-value="true"
     */
    private boolean multiversion;

    /**
     * @parameter expression="${maven.unix.debug}" default-value="false"
     */
    protected boolean debug;

    /**
     * @parameter expression="${maven.unix.dpkg-scanpackages.output}" default-value="target/Packages"
     */
    protected File outputFile;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private String localRepository;

    /**
     * @parameter expression="${basedir}"
     * @readonly
     */
    private File basedir;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( root == null )
        {
            root = new File( localRepository );
        }

        if ( !outputFile.isAbsolute() )
        {
            outputFile = new File( basedir, outputFile.getPath() );
        }

        if ( !outputFile.getParentFile().isDirectory() )
        {
            if ( !outputFile.getParentFile().mkdirs() )
            {
                throw new MojoFailureException( "Could not create parent directories for package file: " + outputFile.getAbsolutePath() );
            }
        }

        OutputStream output = null;

        try
        {
            output = new FileOutputStream( outputFile );

            new SystemCommand().
                dumpCommandIf( debug ).
                withStdoutConsumer( output ).
                withStderrConsumer( System.out ).
                setCommand( "dpkg-scanpackages" ).
                setBasedir( root ).
                addArgumentIf( multiversion, "-m" ).
                addArgument( "." ).
                addArgument( overrideFile ).
                addArgumentIfNotEmpty( pathPrefix ).
                execute().
                assertSuccess();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not run dpkg-scanpackages.", e );
        }
        finally {
            IOUtil.close(output);
        }
    }
}
