package org.codehaus.mojo.unix;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UnixPackage
    implements FileCollector
{
    private final String packageFileExtension;

    private PackageVersion version;
    private File basedir;

    public UnixPackage( String packageFileExtension )
    {
        this.packageFileExtension = packageFileExtension;
    }

    // -----------------------------------------------------------------------
    // Maven Meta Data
    // -----------------------------------------------------------------------

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier
    )
    {
        return this;
    }

    public UnixPackage dependencies( Set dependencies )
    {
        return this;
    }

    // -----------------------------------------------------------------------
    // Generic Meta Data
    // -----------------------------------------------------------------------

    public final UnixPackage version( PackageVersion version )
    {
        this.version = version;

        return this;
    }

    public UnixPackage name( String name )
    {
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        return this;
    }

    public UnixPackage description( String description )
    {
        return this;
    }

    public UnixPackage license( String license )
    {
        return this;
    }

    public UnixPackage contact( String contact )
    {
        return this;
    }

    public UnixPackage contactEmail( String contactEmail )
    {
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        return this;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public UnixPackage workingDirectory( File file )
    {
        return this;
    }

    public UnixPackage basedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public UnixPackage debug( boolean debug )
    {
        return this;
    }

    public abstract void packageToFile( File packageFile )
        throws IOException, MissingSettingException;

    public final PackageVersion getVersion()
    {
        return version;
    }

    public String getPackageFileExtension()
    {
        return packageFileExtension;
    }
}
