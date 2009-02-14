package org.codehaus.mojo.unix;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Represents a Unix package that has attributes and a set of files to be packaged.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
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

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
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

    /**
     * A unique identifier of the package.
     */
    public UnixPackage name( String name )
    {
        return this;
    }

    /**
     * A single-line description of the package.
     */
    public UnixPackage shortDescription( String shortDescription )
    {
        return this;
    }

    /**
     * A multi-line description of the package.
     */
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

    public UnixPackage workingDirectory( FileObject file )
        throws FileSystemException
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
