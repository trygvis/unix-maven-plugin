package org.codehaus.mojo.unix.rpm;

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

import org.codehaus.mojo.unix.util.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Rpmbuild
{
    private boolean debug;

    private File buildroot;

    private File specFile;

    private String rpmbuildPath = "rpmbuild";

    private List<String> defines = new LinkedList<String>();

    public Rpmbuild setDebug( boolean debug )
    {
        this.debug = debug;

        return this;
    }

    public Rpmbuild setBuildroot( File buildroot )
    {
        this.buildroot = buildroot;
        return this;
    }

    public Rpmbuild setSpecFile( File specFile )
    {
        this.specFile = specFile;
        return this;
    }

    public Rpmbuild setRpmbuildPath( String rpmbuildPath )
    {
        this.rpmbuildPath = StringUtils.defaultString( rpmbuildPath, this.rpmbuildPath );
        return this;
    }

    public Rpmbuild define( String define )
    {
        this.defines.add( define );
        return this;
    }

    public void buildBinary()
        throws IOException
    {
        if ( specFile == null )
        {
            throw new IOException( "Package specFile is not set." );
        }

        // Seems like pkgmk doesn't like its stderr/stdout to be closed.
        SystemCommand.StringBufferLineConsumer out = new SystemCommand.StringBufferLineConsumer();

        SystemCommand command = new SystemCommand().
            setBasedir( new File( "/" ) ).
            dumpCommandIf( debug ).
            withStderrConsumer( out ).
            withStdoutConsumer( out ).
            setCommand( rpmbuildPath ).
            addArgument( "-bb" ).
            addArgument( "--buildroot" ).
            addArgument( buildroot.getAbsolutePath() ).
            addArgument( "--target" ).
            addArgument( "noarch" ).
            addArgument( specFile.getAbsolutePath() );

        // TODO: Only the _topdir defines should be there, the others should be in the spec file
        // TODO: This should be configurable
        for ( String define : defines )
        {
            command.
                addArgument( "--define" ).
                addArgument( define );
        }

        SystemCommand.ExecutionResult result = command.
            execute();

        if ( debug )
        {
            System.out.println( "------------------------------------------------------" );
            System.out.println( result.command + " output:" );
            System.out.println( out );
        }

        result.
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "rpmbuild" );
    }
}
