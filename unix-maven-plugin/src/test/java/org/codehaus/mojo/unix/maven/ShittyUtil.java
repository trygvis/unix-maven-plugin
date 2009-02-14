package org.codehaus.mojo.unix.maven;

import groovy.lang.Closure;
import org.codehaus.mojo.unix.EqualsIgnoreNull;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.dpkg.DpkgDebTool;
import org.codehaus.mojo.unix.pkg.PkgchkUtil;
import org.codehaus.mojo.unix.rpm.RpmUtil;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShittyUtil
{
    public static RelativePath r( String path )
    {
        return RelativePath.fromString( path );
    }

    public static boolean assertFormat( String type, String tool, boolean available, Closure closure )
    {
        if ( available )
        {
            System.out.println( "*********************************************************************" );
            System.out.println( "* Running asserts for '" + type + "'" );
            System.out.println( "*********************************************************************" );
            boolean result = (Boolean) closure.call();
            System.out.println( "*********************************************************************" );
            System.out.println( "* Asserts completed for '" + type + "', success: " + result );
            System.out.println( "*********************************************************************" );
            return result;
        }
        else
        {
            System.out.println( "*********************************************************************" );
            System.out.println( "* Skipping asserts for '" + type + "', " + tool + " is not available" );
            System.out.println( "*********************************************************************" );
            return true;
        }
    }

    public static boolean assertRelaxed( EqualsIgnoreNull expected, EqualsIgnoreNull actual )
    {
        boolean ok = expected.equalsIgnoreNull( actual );

        if ( ok )
        {
            return true;
        }

        System.out.println( "*********************************************************************" );
        System.out.println( "*                              Failure                              *" );
        System.out.println( "*********************************************************************" );
        System.out.println( "Expected:" );
        System.out.println( System.getProperty( "line.separator" ) + expected );

        System.out.println();
        System.out.println( "Actual:" );
        System.out.println( System.getProperty( "line.separator" ) + actual );

        return false;
    }

    public static boolean assertDpkgEntries( File pkg, List expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, DpkgDebTool.contents( pkg ), new Checker()
        {
            public boolean equalsIgnoreNull( Object e, Object a )
            {
                UnixFsObject expected = (UnixFsObject) e;
                UnixFsObject actual = (UnixFsObject) a;
                return expected.path.equals( actual.path ) &&
                    ( expected.size == 0 || expected.size == actual.size ) &&
                    ( expected.lastModified == null || expected.lastModified.equals( actual.lastModified ) );
            }

            public String getPath( Object o )
            {
                return ((UnixFsObject)o).path.string;
            }
        } );
    }

    public static boolean assertPkgEntries( File pkg, List expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, PkgchkUtil.getPackageInforForDevice( pkg ), new Checker()
        {
            public boolean equalsIgnoreNull( Object expected, Object actual )
            {
                return ((PkgchkUtil.FileInfo)expected).equalsIgnoreNull( (PkgchkUtil.FileInfo) actual );
            }

            public String getPath( Object o )
            {
                return ((PkgchkUtil.FileInfo)o).pathname;
            }
        } );
    }

    public static boolean assertRpmEntries( File pkg, List expectedFiles )
        throws IOException
    {
        return assertEntries( expectedFiles, RpmUtil.queryPackageForFileInfo( pkg ), new Checker()
        {
            public boolean equalsIgnoreNull( Object expected, Object actual )
            {
                return ((RpmUtil.FileInfo)expected).equalsIgnoreNull( (EqualsIgnoreNull) actual );
            }

            public String getPath( Object o )
            {
                return ((RpmUtil.FileInfo)o).path;
            }
        } );
    }

    public static boolean assertEntries( List expectedFiles, List actualFiles, Checker checker )
        throws IOException
    {
        boolean success = true;

        for ( Iterator it = expectedFiles.iterator(); it.hasNext(); )
        {
            Object expected = it.next();

            int i = actualFiles.indexOf( expected );
            if ( i != -1 )
            {
                Object actual = actualFiles.remove( i );
                if ( !checker.equalsIgnoreNull( expected, actual ) )
                {
                    System.out.println( "Found invalid entry: " + checker.getPath( expected ) );
                    System.out.println( "Expected" );
                    System.out.println( expected.toString() );
                    System.out.println( "Actual" );
                    System.out.println( actual.toString() );
                    success = false;
                }
                else
                {
                    // This output is a bit too noisy
                    // System.out.println( "Found entry: " + expected.pathname );
                }
            }
            else
            {
                success = false;
                System.out.println( "Missing entry: " + checker.getPath( expected ) );
            }
        }

        if ( actualFiles.size() > 0 )
        {
            success = false;
            System.out.println( "Extra files in package:" );

            for ( Iterator it = actualFiles.iterator(); it.hasNext(); )
            {
                System.out.println( "Extra entry: " + checker.getPath( it.next() ) );
            }
        }

        return success;
    }

    private interface Checker
    {
        boolean equalsIgnoreNull( Object expected, Object actual );

        String getPath( Object o);
    }
}
