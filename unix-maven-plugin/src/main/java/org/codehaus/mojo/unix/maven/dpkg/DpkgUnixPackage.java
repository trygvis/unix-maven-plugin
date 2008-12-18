package org.codehaus.mojo.unix.maven.dpkg;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.dpkg.Dpkg;
import org.codehaus.mojo.unix.maven.FsFileCollector;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.util.UnixUtil;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackage
    extends UnixPackage
{
    private ControlFile controlFile = new ControlFile();

    private FsFileCollector fileCollector = new FsFileCollector();

    private File workingDirectory;

    private String dpkgDebPath;

    private boolean debug;

    private final static ScriptUtil scriptUtil;

    static
    {
        scriptUtil = new ScriptUtil.ScriptUtilBuilder().
            format( "dpkg" ).
            setPreInstall( "preinst" ).
            setPostInstall( "postinst" ).
            setPreRemove( "prerm" ).
            setPostRemove( "postrm" ).
            done();
    }

    public DpkgUnixPackage()
    {
        super( "deb" );
    }

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
    {
        controlFile.groupId = groupId;
        controlFile.artifactId = artifactId;

        return this;
    }

    public UnixPackage dependencies( Set dependencies )
    {
        controlFile.dependencies = dependencies;
        return this;
    }

    public UnixPackage name( String name )
    {
        controlFile._package = name;
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        controlFile.shortDescription = shortDescription;
        return this;
    }

    public UnixPackage description( String description )
    {
        controlFile.description = description;
        return this;
    }

    public UnixPackage contact( String contact )
    {
        controlFile.maintainer = contact;
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        controlFile.architecture = architecture;
        return this;
    }

    public UnixPackage workingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public UnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public FileCollector addDirectory( String path, String user, String group, String mode )
        throws IOException
    {
        fileCollector.addDirectory( path, user, group, mode );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
    {
        fileCollector.addFile( fromFile, toFile, user, group, mode );

        return this;
    }

    // -----------------------------------------------------------------------
    // Debian Specifics
    // -----------------------------------------------------------------------

    public DpkgUnixPackage dpkgDeb( String dpkgDeb )
    {
        this.dpkgDebPath = dpkgDeb;
        return this;
    }

    public DpkgUnixPackage section( String secion )
    {
        controlFile.section = secion;
        return this;
    }

    public DpkgUnixPackage priority( String priority )
    {
        controlFile.priority = priority;
        return this;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void packageToFile( File packageFile )
        throws IOException, MissingSettingException
    {
        controlFile.version = getVersion();
        controlFile.toFile( workingDirectory );

        fileCollector.collect( workingDirectory );

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), new File( workingDirectory, "DEBIAN" ) );

        UnixUtil.chmodIf( execution.hasPreInstall(), execution.getPreInstall(), "0755" );
        UnixUtil.chmodIf( execution.hasPostInstall(), execution.getPostInstall(), "0755" );
        UnixUtil.chmodIf( execution.hasPreRemove(), execution.getPreRemove(), "0755" );
        UnixUtil.chmodIf( execution.hasPostRemove(), execution.getPostRemove(), "0755" );

        new Dpkg().
            setDebug( debug ).
            setPackageRoot( workingDirectory ).
            setDebFile( packageFile ).
            setDpkgDebPath( dpkgDebPath ).
            execute();
    }

    public static DpkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (DpkgUnixPackage) unixPackage;
    }
}
