package org.codehaus.mojo.unix.deb;

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

/**
 * Builds a DEB package using fakeroot dpkg-deb.  This command creates a DEB
 * package in buildDir using the contents of stageDir.  This classes
 * assumes that stageDir is going to be a direct descendent of buildDir.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Dpkg
{
    private boolean debug;

    private File packageRoot;

    private File debFile;

    private boolean useFakeroot;

    private String dpkgDebPath = "dpkg-deb";

    public Dpkg setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public Dpkg setPackageRoot( File packageRoot )
    {
        this.packageRoot = packageRoot;
        return this;
    }

    public Dpkg setDebFile( File debFile )
    {
        this.debFile = debFile;
        return this;
    }

    public Dpkg setUseFakeroot( boolean useFakeroot )
    {
        this.useFakeroot = useFakeroot;
        return this;
    }

    public Dpkg setDpkgDebPath( String dpkgDebPath )
    {
        this.dpkgDebPath = StringUtils.defaultString( dpkgDebPath, this.dpkgDebPath );
        return this;
    }

    public void execute()
        throws IOException
    {
        if ( packageRoot == null )
        {
            throw new IOException( "Package root is not set." );
        }

        if ( debFile == null )
        {
            throw new IOException( "Path to output .deb is not set." );
        }

        new SystemCommand().
            setCommand( useFakeroot ? "fakeroot" : dpkgDebPath ).
            dumpCommandIf( debug ).
            withNoStderrConsumerUnless( debug ).
            withNoStdoutConsumerUnless( debug ).
            addArgumentIf( useFakeroot, dpkgDebPath ).
            addArgument( "-b" ).
            addArgument( packageRoot.getAbsolutePath() ).
            addArgument( debFile.getAbsolutePath() ).
            execute().
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "dpkg" );
    }
}
