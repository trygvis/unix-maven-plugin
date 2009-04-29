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

import fj.*;
import fj.data.*;
import static fj.data.HashMap.*;
import static fj.data.Option.*;
import static fj.data.Stream.*;
import groovy.lang.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.FileAttributes;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.dpkg.*;
import org.codehaus.mojo.unix.pkg.*;
import org.codehaus.mojo.unix.pkg.prototype.*;
import org.codehaus.mojo.unix.rpm.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import static org.codehaus.mojo.unix.util.UnixUtil.*;
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;

import java.io.*;
import java.util.zip.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShittyUtil
{
    public static final LocalDateTime START_OF_TIME = PrototypeFile.START_OF_TIME;

    public static final OutputStreamWriter out = new OutputStreamWriter( System.out );

    public static final LineWriterWriter stream = new LineWriterWriter( out );

    public static RelativePath r( String path )
    {
        return relativePath( path );
    }

    // -----------------------------------------------------------------------
    // Misc Utils
    // -----------------------------------------------------------------------

    public static File findArtifact( String groupId, String artifactId, String version, String type )
        throws IOException
    {
        return findArtifact( groupId, artifactId, version, type, null );
    }

    public static File findArtifact(String groupId, String artifactId, String version, String type, String classifier)
        throws IOException
    {
        File m2Repository = new File( System.getProperty( "user.home" ), ".m2/repository" );

        // TODO: This can be improved
        if ( !m2Repository.isDirectory() )
        {
            throw new IOException( "Unable to find local repository: " + m2Repository.getAbsolutePath() );
        }

        String base = groupId.replace( '/', '.' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version;

        if ( classifier != null )
        {
            base += "-" + classifier;
        }

        return new File( m2Repository, base + "." + type );
    }

    // -----------------------------------------------------------------------
    // Assertion tools
    // -----------------------------------------------------------------------

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

    public static boolean assertDpkgEntries( File pkg, java.util.List<UnixFsObject> expectedFiles )
        throws IOException
    {
        final HashMap<RelativePath, UnixFsObject> map = hashMap();
        iterableStream( DpkgDebTool.contents( pkg ) ).foreach( new Effect<UnixFsObject>()
        {
            public void e( UnixFsObject unixFsObject )
            {
                map.set( unixFsObject.path, unixFsObject );
            }
        } );

        return assertEntries( iterableStream( expectedFiles ).map( unixFsObjectToP2 ), map, new UnixFsObjectChecker() );
    }

    public static boolean assertPkgEntries( File pkg, java.util.List<PkgchkUtil.FileInfo> expectedFiles )
        throws IOException
    {
        final HashMap<RelativePath, PkgchkUtil.FileInfo> map = hashMap();
        PkgchkUtil.getPackageInforForDevice( pkg ).foreach( new Effect<PkgchkUtil.FileInfo>()
        {
            public void e( PkgchkUtil.FileInfo fileInfo )
            {
                map.set( relativePath( fileInfo.pathname ), fileInfo );
            }
        } );

        F<PkgchkUtil.FileInfo, P2<RelativePath, PkgchkUtil.FileInfo>> fileInfoToP2 =
            new F<PkgchkUtil.FileInfo, P2<RelativePath, PkgchkUtil.FileInfo>>()
            {
                public P2<RelativePath, PkgchkUtil.FileInfo> f( PkgchkUtil.FileInfo fileInfo )
                {
                    return P.p( relativePath( fileInfo.pathname ), fileInfo );
                }
            };

        F2<PkgchkUtil.FileInfo, PkgchkUtil.FileInfo, Boolean> fileInfoChecker = new F2<PkgchkUtil.FileInfo, PkgchkUtil.FileInfo, Boolean>()
        {
            public Boolean f( PkgchkUtil.FileInfo expected, PkgchkUtil.FileInfo actual )
            {
                return expected.equalsIgnoreNull( actual );
            }
        };

        return assertEntries( iterableStream( expectedFiles ).map( fileInfoToP2 ), map, fileInfoChecker );
    }

    public static boolean assertRpmEntries( File pkg, java.util.List<RpmUtil.FileInfo> expectedFiles )
        throws IOException
    {
        final HashMap<RelativePath, RpmUtil.FileInfo> map = hashMap();

        F<RpmUtil.FileInfo, P2<RelativePath, RpmUtil.FileInfo>> fileInfoToP2 =
            new F<RpmUtil.FileInfo, P2<RelativePath, RpmUtil.FileInfo>>()
            {
                public P2<RelativePath, RpmUtil.FileInfo> f( RpmUtil.FileInfo fileInfo )
                {
                    return P.p( relativePath( fileInfo.path ), fileInfo );
                }
            };

        iterableStream( RpmUtil.queryPackageForFileInfo( pkg ) ).foreach(
            new Effect<org.codehaus.mojo.unix.rpm.RpmUtil.FileInfo>()
            {
                public void e( org.codehaus.mojo.unix.rpm.RpmUtil.FileInfo fileInfo )
                {
                    map.set( relativePath( fileInfo.path ), fileInfo );
                }
            } );

        F2<RpmUtil.FileInfo, RpmUtil.FileInfo, Boolean> fileInfoChecker = new F2<RpmUtil.FileInfo, RpmUtil.FileInfo, Boolean>()
            {
                public Boolean f( RpmUtil.FileInfo expected, RpmUtil.FileInfo actual )
                {
                    return expected.equalsIgnoreNull( actual );
                }
            };

        return assertEntries( iterableStream( expectedFiles ).map( fileInfoToP2 ), map, fileInfoChecker );
    }

    public static boolean assertZipEntries( File zip, java.util.List<UnixFsObject> expectedFiles )
        throws IOException
    {
        HashMap<RelativePath, UnixFsObject> actualFiles = hashMap();

        ZipInputStream zis = null;
        try
        {
            zis = new ZipInputStream( new FileInputStream( zip ) );

            ZipEntry zipEntry = zis.getNextEntry();

            while ( zipEntry != null )
            {
                LocalDateTime time = new LocalDateTime( zipEntry.getTime() );
                RelativePath path = relativePath( zipEntry.getName() );
                UnixFsObject o;
                if ( zipEntry.isDirectory() )
                {
                    o = directory( path, time, FileAttributes.EMPTY );
                }
                else
                {
                    long size = zipEntry.getSize();

                    // For some reason ZipInputStream can't give me zipEntry objects with a reasonable getSize()
                    if(size == -1) {
                        size = 0;
                        int s;

                        while ( true )
                        {
                            byte[] bytes = new byte[1024 * 128];
                            s = zis.read( bytes, 0, bytes.length );
                            if ( s == -1 )
                            {
                                break;
                            }
                            size += s;
                        }

                    }

                    o = regularFile( path, time, size, some( FileAttributes.EMPTY ) );
                }
                actualFiles.set( path, o );
                zipEntry = zis.getNextEntry();
            }
        }
        finally
        {
            IOUtil.close( zis );
        }

        return assertEntries( iterableStream( expectedFiles ).map( unixFsObjectToP2 ), actualFiles,
                              new UnixFsObjectChecker() );
    }

    public static <T extends LineProducer> boolean assertEntries( Stream<P2<RelativePath, T>> expectedFiles,
                                                                  HashMap<RelativePath, T> actualFiles,
                                                                  F2<T, T, Boolean> checker )
        throws IOException
    {
        boolean success = true;

        for ( P2<RelativePath, T> p2 : expectedFiles )
        {
            RelativePath expectedPath = p2._1();
            Option<T> actualO = actualFiles.get( expectedPath );
            if ( actualO.isSome() )
            {
                T actual = actualO.some();
                T expected = p2._2();
                if ( !checker.f( expected, actual ) )
                {
                    stream.add( "Found invalid entry: " + expectedPath );
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

                actualFiles.delete( expectedPath );
            }
            else
            {
                success = false;
                stream.add( "Missing entry: " + expectedPath );
            }
        }

        if ( actualFiles.size() > 0 )
        {
            success = false;
            stream.add( "Extra files in package:" );

            for ( RelativePath path : actualFiles.keys() )
            {
                stream.add( "Extra entry: " + path );
            }
        }

        stream.flush();
        return success;
    }

    private static class UnixFsObjectChecker
        implements F2<UnixFsObject, UnixFsObject,  Boolean>
    {
        public Boolean f( UnixFsObject expected, UnixFsObject actual )
        {
            return expected.path.equals( actual.path ) && ( expected.size == 0 || expected.size == actual.size ) &&
                ( expected.lastModified == null || expected.lastModified.equals( START_OF_TIME ) ||
                    expected.lastModified.equals( actual.lastModified ) );
        }
    }

    static F<UnixFsObject, P2<RelativePath, UnixFsObject>> unixFsObjectToP2 =
        new F<UnixFsObject, P2<RelativePath, UnixFsObject>>()
        {
            public P2<RelativePath, UnixFsObject> f( UnixFsObject unixFsObject )
            {
                return P.p( unixFsObject.path, unixFsObject );
            }
        };
}
