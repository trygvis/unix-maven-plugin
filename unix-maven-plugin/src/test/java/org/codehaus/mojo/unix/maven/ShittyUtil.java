package org.codehaus.mojo.unix.maven;

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

import groovy.lang.Closure;
import org.codehaus.mojo.unix.EqualsIgnoreNull;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.dpkg.DpkgDebTool;
import org.codehaus.mojo.unix.pkg.PkgchkUtil;
import org.codehaus.mojo.unix.rpm.RpmUtil;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.UnixUtil.flush;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineWriterWriter;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShittyUtil
{
    public static final LocalDateTime START_OF_TIME = new LocalDateTime( 1970, 1, 1, 0, 0 );
    public static final OutputStreamWriter out = new OutputStreamWriter( System.out );
    public static final LineWriterWriter stream = new LineWriterWriter( out );

    public static RelativePath r( String path )
    {
        return RelativePath.fromString( path );
    }

    public static boolean assertFormat( String type, String tool, boolean available, Closure closure )
    {
        boolean result;

        if ( available )
        {
            stream.add( "*********************************************************************" );
            stream.add( "* Running asserts for '" + type + "'" );
            stream.add( "*********************************************************************" );
            result = (Boolean) closure.call();
            stream.add( "*********************************************************************" );
            stream.add( "* Asserts completed for '" + type + "', success: " + result );
            stream.add( "*********************************************************************" );
        }
        else
        {
            stream.add( "*********************************************************************" );
            stream.add( "* Skipping asserts for '" + type + "', " + tool + " is not available" );
            stream.add( "*********************************************************************" );
            result = true;
        }

        flush( stream );
        return result;
    }

    public static <T extends LineProducer & EqualsIgnoreNull<T>> boolean assertRelaxed( T expected, T actual )
    {
        boolean ok = expected.equalsIgnoreNull( actual );

        if ( ok )
        {
            return true;
        }

        stream.add( "*********************************************************************" );
        stream.add( "*                              Failure                              *" );
        stream.add( "*********************************************************************" );
        stream.add( "Expected:" );
        stream.add();
        expected.streamTo( stream );

        stream.add();
        stream.add( "Actual:" );
        stream.add();
        actual.streamTo( stream );
        flush( out );

        return false;
    }

    public static boolean assertDpkgEntries( File pkg, List<UnixFsObject> expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, DpkgDebTool.contents( pkg ), new Checker<UnixFsObject>()
        {
            public boolean equalsIgnoreNull( UnixFsObject expected, UnixFsObject actual )
            {
                return expected.path.equals( actual.path ) &&
                    ( expected.size == 0 || expected.size == actual.size ) &&
                    ( expected.lastModified == null || expected.lastModified.equals( START_OF_TIME ) || expected.lastModified.equals( actual.lastModified ) );
            }

            public String getPath( UnixFsObject o )
            {
                return o.path.string;
            }
        } );
    }

    public static boolean assertPkgEntries( File pkg, List<PkgchkUtil.FileInfo> expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, new ArrayList<PkgchkUtil.FileInfo>( PkgchkUtil.getPackageInforForDevice( pkg ).toCollection() ), new Checker<PkgchkUtil.FileInfo>()
        {
            public boolean equalsIgnoreNull( PkgchkUtil.FileInfo expected, PkgchkUtil.FileInfo actual )
            {
                return expected.equalsIgnoreNull( actual );
            }

            public String getPath( PkgchkUtil.FileInfo o )
            {
                return o.pathname;
            }
        } );
    }

    public static boolean assertRpmEntries( File pkg, List<RpmUtil.FileInfo> expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, RpmUtil.queryPackageForFileInfo( pkg ), new Checker<RpmUtil.FileInfo>()
        {
            public boolean equalsIgnoreNull( RpmUtil.FileInfo expected, RpmUtil.FileInfo actual )
            {
                return expected.equalsIgnoreNull( actual );
            }

            public String getPath( RpmUtil.FileInfo o )
            {
                return o.path;
            }
        } );
    }

    public static <T extends LineProducer>  boolean assertEntries( List<T> expectedFiles, List<T> actualFiles, Checker<T> checker )
        throws IOException
    {
        boolean success = true;

        for (T expected : expectedFiles)
        {
            int i = actualFiles.indexOf( expected );
            if ( i != -1 )
            {
                T actual = actualFiles.remove( i );
                if ( !checker.equalsIgnoreNull( expected, actual ) )
                {
                    stream.add( "Found invalid entry: " + checker.getPath( expected ) );
                    stream.add( "Expected" );
                    expected.streamTo( stream );
                    stream.add( "Actual" );
                    actual.streamTo( stream );
                    success = false;
                }
                else
                {
                    // This output is a bit too noisy
                    // stream.add( "Found entry: " + expected.pathname );
                }
            }
            else
            {
                success = false;
                stream.add( "Missing entry: " + checker.getPath( expected ) );
            }
        }

        if ( actualFiles.size() > 0 )
        {
            success = false;
            stream.add( "Extra files in package:" );

            for ( T actualFile : actualFiles )
            {
                stream.add( "Extra entry: " + checker.getPath( actualFile ) );
            }
        }

        stream.flush();
        return success;
    }

    private interface Checker<T>
    {
        boolean equalsIgnoreNull( T expected, T actual );

        String getPath( T o);
    }
}
