package org.codehaus.mojo.unix.maven.pkg;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.maven.pkg.prototype.PrototypeFile;
import org.codehaus.mojo.unix.pkg.PkgmkCommand;
import org.codehaus.mojo.unix.pkg.PkgtransCommand;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgUnixPackage
    extends UnixPackage
{
    private File workingDirectory;
    private boolean debug;
    private final static ScriptUtil scriptUtil;

    static
    {
        scriptUtil = new ScriptUtil.ScriptUtilBuilder().
            format( "pkg" ).
            setPreInstall( "preinstall" ).
            setPostInstall( "postinstall" ).
            setPreRemove( "preremove" ).
            setPostRemove( "postremove" ).
            done();
    }

    private final PrototypeFile prototypeFile = new PrototypeFile();
    private final PkginfoFile pkginfoFile = new PkginfoFile();

    public PkgUnixPackage()
    {
        super( "pkg" );
    }

    // -----------------------------------------------------------------------
    // Common Settings
    // -----------------------------------------------------------------------

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
    {
        pkginfoFile.classifier = classifier;
        return this;
    }

    public UnixPackage name( String name )
    {
        pkginfoFile.packageName = name;
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        pkginfoFile.name = shortDescription;
        return this;
    }

    public UnixPackage description( String description )
    {
        pkginfoFile.description = description;
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        pkginfoFile.arch = architecture;
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

    // -----------------------------------------------------------------------
    // Pkg Specific Settings
    // -----------------------------------------------------------------------

    public void classes( String classes )
    {
        pkginfoFile.classes = classes;
    }

    public void packageToFile( File packageFile )
        throws IOException, MissingSettingException
    {
        File prototype = new File( workingDirectory, "prototype" );
        File pkginfo = new File( workingDirectory, "pkginfo" );

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), workingDirectory );

        pkginfoFile.version = getVersion();
        pkginfoFile.writeTo( pkginfo );

        String pkg = pkginfoFile.getPkgName( pkginfo );

        prototypeFile.includeFileIf( pkginfo, "pkginfo" );
        prototypeFile.includeFileIf( execution.getPreInstall(), "preinstall" );
        prototypeFile.includeFileIf( execution.getPostInstall(), "postinstall" );
        prototypeFile.includeFileIf( execution.getPreRemove(), "preremove" );
        prototypeFile.includeFileIf( execution.getPostRemove(), "postremove" );
        prototypeFile.writeTo( prototype );

        new PkgmkCommand().
            setDebug( debug ).
            setOverwrite( true ).
            setDevice( workingDirectory ).
            setPrototype( prototype ).
            execute();

        new PkgtransCommand().
            setDebug( debug ).
            setAsDatastream( true ).
            setOverwrite( true ).
            execute( workingDirectory, packageFile, pkg );
    }

    public FileCollector addDirectory( String path, String user, String group, String mode )
        throws IOException
    {
        prototypeFile.addDirectory( path, user, group, mode );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
        throws IOException
    {
        prototypeFile.addFile( fromFile, toFile, user, group, mode );

        return this;
    }

    public static PkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (PkgUnixPackage) unixPackage;
    }
}
