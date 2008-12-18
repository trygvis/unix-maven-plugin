package org.codehaus.mojo.unix.maven.dpkg;

import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.PackagingFormat;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgPackagingFormat
    implements PackagingFormat
{
    public UnixPackage start()
    {
        return new DpkgUnixPackage();
    }

    public boolean licenseRequired()
    {
        return false;
    }

    public String defaultArchitecture()
    {
        return "all";
    }
}
