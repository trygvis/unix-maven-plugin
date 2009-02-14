package org.codehaus.mojo.unix.maven.rpm;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.FsFileCollector;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.rpm.Rpmbuild;
import org.codehaus.mojo.unix.rpm.SpecFile;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUnixPackage
    extends UnixPackage
{
    private SpecFile specFile = new SpecFile();

    private RpmTool rpmTool = new RpmTool();

    private FsFileCollector fileCollector;

    private File workingDirectoryF;

    private String rpmbuildPath;

    private boolean debug;

    private final static ScriptUtil scriptUtil;

    static
    {
        scriptUtil = new ScriptUtil.ScriptUtilBuilder().
            format( "rpm" ).
            setPreInstall( "pre-install" ).
            setPostInstall( "post-install" ).
            setPreRemove( "pre-remove" ).
            setPostRemove( "post-remove" ).
            build();
    }

    public RpmUnixPackage()
    {
        super( "rpm" );
    }

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
    {
        specFile.groupId = groupId;
        specFile.artifactId = artifactId;

        rpmTool.groupId = groupId;
        rpmTool.artifactId = artifactId;
        return this;
    }

    public UnixPackage name( String name )
    {
        specFile.name = name;
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        specFile.summary = shortDescription;
        return this;
    }

    public UnixPackage description( String description )
    {
        specFile.description = description;
        return this;
    }

    public UnixPackage license( String license )
    {
        specFile.license = license;
        return this;
    }

    public UnixPackage group( String group )
    {
        specFile.group = group;
        return this;
    }

    public UnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        workingDirectoryF = VfsUtil.asFile( workingDirectory );
        fileCollector = new FsFileCollector( workingDirectory.resolveFile( "assembly" ) );
        return this;
    }

    public UnixPackage debug( boolean debug )
    {
        this.specFile.dump = debug;
        this.fileCollector.debug( debug );
        this.debug = debug;
        return this;
    }

    // TODO: This is not used
    public UnixPackage rpmbuildPath( String rpmbuildPath )
    {
        this.rpmbuildPath = rpmbuildPath;
        return this;
    }

    public FileObject getRoot()
    {
        return fileCollector.getRoot();
    }

    public FileCollector addDirectory( RelativePath path, FileAttributes attributes )
        throws IOException
    {
        specFile.addDirectory( path, attributes );
        fileCollector.addDirectory( path, attributes );
        return this;
    }

    public FileCollector addFile( FileObject fromFile, RelativePath toPath, FileAttributes attributes )
        throws IOException
    {
        specFile.addFile( toPath, attributes );
        fileCollector.addFile( fromFile, toPath, attributes );
        return this;
    }

    public void packageToFile( File packageFile )
        throws IOException, MissingSettingException
    {
        File rpms = new File( workingDirectoryF, "RPMS" );
        File specsDir = new File( workingDirectoryF, "SPECS" );
        File tmp = new File( workingDirectoryF, "tmp" );

        File specFilePath = new File( specsDir, rpmTool.getBaseName() + ".spec" );

        FileUtils.forceMkdir( new File( workingDirectoryF, "BUILD" ) );
        FileUtils.forceMkdir( rpms );
        FileUtils.forceMkdir( new File( workingDirectoryF, "SOURCES" ) );
        FileUtils.forceMkdir( specsDir );
        FileUtils.forceMkdir( new File( workingDirectoryF, "SRPMS" ) );
        FileUtils.forceMkdir( tmp );

        fileCollector.collect();

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), new File( workingDirectoryF, "scripts" ) );

        specFile.includePre = execution.getPreInstall();
        specFile.includePost = execution.getPostInstall();
        specFile.includePreun = execution.getPreRemove();
        specFile.includePostun = execution.getPostRemove();
        specFile.version = getVersion();
        specFile.buildRoot = VfsUtil.asFile( fileCollector.getFsRoot() );
        specFile.writeToFile( specFilePath );

        new Rpmbuild().
            setDebug( debug ).
            setBuildroot( VfsUtil.asFile( fileCollector.getFsRoot() ) ).
            define( "_tmppath " + tmp.getAbsolutePath() ).
            define( "_topdir " + workingDirectoryF.getAbsolutePath() ).
            define( "_rpmdir " + packageFile.getParentFile().getAbsolutePath() ).
            define( "_rpmfilename " + packageFile.getName() ).
            setSpecFile( specFilePath ).
            setRpmbuildPath( rpmbuildPath ).
            buildBinary();
    }

    public static RpmUnixPackage cast( UnixPackage unixPackage )
    {
        return (RpmUnixPackage) unixPackage;
    }
}
