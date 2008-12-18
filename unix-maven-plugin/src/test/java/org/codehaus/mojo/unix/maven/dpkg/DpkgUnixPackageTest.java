package org.codehaus.mojo.unix.maven.dpkg;

import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.dpkg.Dpkg;
import org.codehaus.mojo.unix.maven.PackagingFormat;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackageTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        if ( !Dpkg.available() )
        {
            return;
        }

        DpkgPackagingFormat packagingFormat = (DpkgPackagingFormat) lookup( PackagingFormat.ROLE, "dpkg" );

        File packageRoot = getTestFile( "target/dpkg-test/root" );
        File packageFile = getTestFile( "target/dpkg-test/file.deb" );

        DpkgUnixPackage.cast( packagingFormat.start() ).
            section( "devel" ).
            debug( true ).
            mavenCoordinates( "mygroup", "myartifact", null ).
            version( PackageVersion.create( "1.0", "123", false, null, new Integer( 1 ) ) ).
            contact( "Kurt Cobain" ).
            architecture( "all" ).
            workingDirectory( packageRoot ).
            packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }
}
