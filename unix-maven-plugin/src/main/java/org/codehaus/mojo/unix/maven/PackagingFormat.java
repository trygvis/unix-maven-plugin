package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.UnixPackage;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface PackagingFormat
{
    String ROLE = PackagingFormat.class.getName();

    UnixPackage start();

    boolean licenseRequired();

    String defaultArchitecture();
}
