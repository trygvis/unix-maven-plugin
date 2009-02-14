package org.codehaus.mojo.unix.maven.pkg;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.maven.pkg.prototype.PrototypeFile;
import org.codehaus.mojo.unix.pkg.PkgmkCommand;
import org.codehaus.mojo.unix.pkg.PkgtransCommand;
import org.codehaus.mojo.unix.pkg.PkginfoFile;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.line.LineStreamUtil;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgUnixPackage
    extends UnixPackage
{
    private FileObject workingDirectory;
    private FileObject prototype;
    private FileObject pkginfo;
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
            build();
    }

    private PrototypeFile prototypeFile;
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
        pkginfoFile.desc = description;
        return this;
    }

    public UnixPackage contactEmail( String contactEmail )
    {
        pkginfoFile.email = contactEmail;
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        pkginfoFile.arch = architecture;
        return this;
    }

    public UnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        this.workingDirectory = workingDirectory;
        prototype = workingDirectory.resolveFile( "prototype" );
        pkginfo = workingDirectory.resolveFile( "pkginfo" );
        prototypeFile = new PrototypeFile( workingDirectory.resolveFile( "assembly" ) );
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
        // -----------------------------------------------------------------------
        // Validate that the prototype looks sane
        // -----------------------------------------------------------------------

        // TODO: This should be more configurable
        FileAttributes unknown = new FileAttributes( "?", "?", null );
        String[] specialPaths = new String[]{
            "/",
            "/etc",
            "/opt",
            "/usr",
            "/var",
            "/var/opt",
        };
        for ( int i = 0; i < specialPaths.length; i++ )
        {
            if ( prototypeFile.hasPath( specialPaths[i] ) )
            {
                prototypeFile.addDirectory( RelativePath.fromString( specialPaths[i] ), unknown );
            }
        }

        // -----------------------------------------------------------------------
        // The shit
        // -----------------------------------------------------------------------

        File workingDirectoryF = VfsUtil.asFile( workingDirectory );
        File pkginfoF = VfsUtil.asFile( pkginfo );
        File prototypeF = VfsUtil.asFile( prototype );

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), workingDirectoryF );
        pkginfoFile.version = getVersion().getMavenVersion();
        pkginfoFile.pstamp = getVersion().timestamp;
        LineStreamUtil.toFile( pkginfoFile, pkginfoF );

        String pkg = pkginfoFile.getPkgName( pkginfoF );

        prototypeFile.includeFileIf( pkginfoF, "pkginfo" );
        prototypeFile.includeFileIf( execution.getPreInstall(), "preinstall" );
        prototypeFile.includeFileIf( execution.getPostInstall(), "postinstall" );
        prototypeFile.includeFileIf( execution.getPreRemove(), "preremove" );
        prototypeFile.includeFileIf( execution.getPostRemove(), "postremove" );
        prototypeFile.toLineFile().writeTo( prototypeF );

        new PkgmkCommand().
            setDebug( debug ).
            setOverwrite( true ).
            setDevice( workingDirectoryF ).
            setPrototype( prototypeF ).
            execute();

        new PkgtransCommand().
            setDebug( debug ).
            setAsDatastream( true ).
            setOverwrite( true ).
            execute( workingDirectoryF, packageFile, pkg );

        prototypeFile.cleanUp();
    }

    public FileObject getRoot()
    {
        return prototypeFile.getRoot();
    }

    public FileCollector addDirectory( RelativePath path, FileAttributes attributes )
        throws IOException
    {
        prototypeFile.addDirectory( path, attributes );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, RelativePath toPath, FileAttributes attributes )
        throws IOException
    {
        prototypeFile.addFile( fromFile, toPath, attributes );

        return this;
    }

    public static PkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (PkgUnixPackage) unixPackage;
    }
}
