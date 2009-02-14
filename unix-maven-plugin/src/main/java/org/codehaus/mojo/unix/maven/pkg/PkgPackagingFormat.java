package org.codehaus.mojo.unix.maven.pkg;

import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.PackagingFormat;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgPackagingFormat
    implements PackagingFormat
{
    public UnixPackage start()
    {
        return new PkgUnixPackage();
    }

    public boolean licenseRequired()
    {
        return false;
    }

    public String defaultArchitecture()
    {
        return "all";
    }

    public static PkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (PkgUnixPackage) unixPackage;
    }
}
