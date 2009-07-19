package org.codehaus.mojo.unix.sysvpkg;

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

import fj.data.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoUtil
{
    public boolean debug;

    public PkginfoUtil()
    {
    }

    public PkginfoUtil( boolean debug )
    {
        this.debug = debug;
    }

    public PkginfoUtil debug()
    {
        return new PkginfoUtil( true );
    }

    public Option<PkginfoFile> getPackageInfoForDevice2( File device )
        throws IOException
    {
        return getPackageInfoForDevice2( device, "all" );
    }

    public Option<PkginfoFile> getPackageInfoForDevice2( File device, String instance )
        throws IOException
    {
        if ( !device.canRead() )
        {
            throw new FileNotFoundException( device.getAbsolutePath() );
        }

        SystemCommand.StringListLineConsumer consumer = new SystemCommand.StringListLineConsumer();

        new SystemCommand().
            dumpCommandIf( debug ).
            withStderrConsumer( consumer ).
            withStdoutConsumer( consumer ).
            setCommand( "pkginfo" ).
            addArgument( "-d" ).
            addArgument( device.getAbsolutePath() ).
            addArgument( "-l" ).
            addArgumentIfNotEmpty( instance ).
            execute().
            assertSuccess();

        return PkginfoFile.fromStream( Stream.iterableStream( consumer.strings ) );
    }

    public static Option<PkginfoFile> getPackageInfoForDevice( File device )
        throws IOException
    {
        return getPackageInfoForDevice( device, null );
    }

    /**
     * @deprecated
     */
    public static Option<PkginfoFile> getPackageInfoForDevice( File device, String instance )
        throws IOException
    {
        return new PkginfoUtil().debug().getPackageInfoForDevice2( device, instance );
    }
}
