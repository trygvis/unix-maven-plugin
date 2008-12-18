package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.pkg.PkgchkUtil;
import org.codehaus.mojo.unix.rpm.RpmUtil;
import org.codehaus.mojo.unix.HasRelaxedEquality;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShittyUtil
{
    public static boolean assertRelaxed( HasRelaxedEquality expected, HasRelaxedEquality actual )
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

    public static boolean assertRpmEntries( File pkg, List expectedFiles )
        throws IOException
    {
        boolean success = true;

        List actualFiles = RpmUtil.queryPackageForFileInfo( pkg );

        for ( Iterator it = expectedFiles.iterator(); it.hasNext(); )
        {
            RpmUtil.FileInfo expected = (RpmUtil.FileInfo) it.next();

            int i = actualFiles.indexOf( expected );
            if ( i != -1 )
            {
                RpmUtil.FileInfo actual = (RpmUtil.FileInfo) actualFiles.remove( i );
                if ( expected.equalsIgnoreNull( actual ) )
                {
                    System.out.println( "Found entry: " + expected.path );
                }
                else
                {
                    System.out.println( "Found invalid entry: " + expected.path );
                    System.out.println( "Expected" );
                    System.out.println( expected.toString() );
                    System.out.println( "Actual" );
                    System.out.println( actual.toString() );
                    success = false;
                }
            }
            else
            {
                success = false;
                System.out.println( "Missing entry: " + expected.path );
            }
        }

        if ( actualFiles.size() > 0 )
        {
            success = false;
            System.out.println( "Extra files in package:" );

            for ( Iterator it = actualFiles.iterator(); it.hasNext(); )
            {
                RpmUtil.FileInfo file = (RpmUtil.FileInfo) it.next();

                System.out.println( "Extra entry: " + file.path );
            }
        }

        return success;
    }

    public static boolean assertPkgEntries( File pkg, List expectedFiles )
        throws IOException
    {
        boolean success = true;

        List actualFiles = PkgchkUtil.getPackageInforForDevice( pkg );

        for ( Iterator it = expectedFiles.iterator(); it.hasNext(); )
        {
            PkgchkUtil.FileInfo expected = (PkgchkUtil.FileInfo) it.next();

            int i = actualFiles.indexOf( expected );
            if ( i != -1 )
            {
                PkgchkUtil.FileInfo actual = (PkgchkUtil.FileInfo) actualFiles.remove( i );
                if ( !expected.equalsIgnoreNull( actual ) )
                {
                    System.out.println( "Found invalid entry: " + expected.pathname );
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
                System.out.println( "Missing entry: " + expected.pathname );
            }
        }

        if ( actualFiles.size() > 0 )
        {
            success = false;
            System.out.println( "Extra files in package:" );

            for ( Iterator it = actualFiles.iterator(); it.hasNext(); )
            {
                PkgchkUtil.FileInfo file = (PkgchkUtil.FileInfo) it.next();

                System.out.println( "Extra entry: " + file.pathname );
            }
        }

        return success;
    }
}
