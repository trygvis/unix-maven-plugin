package org.codehaus.mojo.unix.maven.rpm;

import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.PackagingFormat;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmPackagingFormat
    implements PackagingFormat
{
    public UnixPackage start()
    {
        return new RpmUnixPackage();
    }

    public boolean licenseRequired()
    {
        return true;
    }

    public String defaultArchitecture()
    {
        return "all";
    }

    public static RpmUnixPackage cast( UnixPackage unixPackage )
    {
        return (RpmUnixPackage) unixPackage;
    }
}
