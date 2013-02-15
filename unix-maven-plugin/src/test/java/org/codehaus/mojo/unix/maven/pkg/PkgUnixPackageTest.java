package org.codehaus.mojo.unix.maven.pkg;

import org.codehaus.mojo.unix.maven.*;
import org.codehaus.mojo.unix.maven.plugin.*;
import org.codehaus.mojo.unix.maven.sysvpkg.*;
import org.codehaus.plexus.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkgUnixPackageTest
    extends PlexusTestCase
{
    public void testFiltering()
        throws Exception
    {
        SysvPkgPackagingFormat packagingFormat = (SysvPkgPackagingFormat) lookup( PackagingFormat.ROLE, "sysvpkg" );

        new UnixPackageTestUtil<PkgUnixPackage, PkgUnixPackage.PkgPreparedPackage>( "pkg", packagingFormat ).testFiltering();
    }
}
