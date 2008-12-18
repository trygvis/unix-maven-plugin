package org.codehaus.mojo.unix.maven.rpm;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.FsFileCollector;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.rpm.Rpmbuild;
import org.codehaus.mojo.unix.rpm.SpecFile;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUnixPackage
    extends UnixPackage
{
    private SpecFile specFile = new SpecFile();

    private RpmTool rpmTool = new RpmTool();

    private FsFileCollector fileCollector = new FsFileCollector();

    private File workingDirectory;

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
            done();
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

    public UnixPackage workingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
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

    public FileCollector addDirectory( String path, String user, String group, String mode )
        throws IOException
    {
        specFile.addDirectory( path, user, group, mode );
        fileCollector.addDirectory( path, user, group, mode );
        return this;
    }

    public FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
        throws IOException
    {
        specFile.addFile( toFile, user, group, mode );
        fileCollector.addFile( fromFile, toFile, user, group, mode );
        return this;
    }

    public void packageToFile( File packageFile )
        throws IOException, MissingSettingException
    {
        File rpms = new File( workingDirectory, "RPMS" );
        File specsDir = new File( workingDirectory, "SPECS" );
        File packageRoot = new File( workingDirectory, "package-root" );
        File tmp = new File( workingDirectory, "tmp" );

        File specFilePath = new File( specsDir, rpmTool.getBaseName() + ".spec" );

        FileUtils.forceMkdir( new File( workingDirectory, "BUILD" ) );
        FileUtils.forceMkdir( rpms );
        FileUtils.forceMkdir( new File( workingDirectory, "SOURCES" ) );
        FileUtils.forceMkdir( specsDir );
        FileUtils.forceMkdir( new File( workingDirectory, "SRPMS" ) );
        FileUtils.forceMkdir( packageRoot );
        FileUtils.forceMkdir( tmp );

        fileCollector.collect( packageRoot );

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), new File( workingDirectory, "scripts" ) );

        specFile.includePre = execution.getPreInstall();
        specFile.includePost = execution.getPostInstall();
        specFile.includePreun = execution.getPreRemove();
        specFile.includePostun = execution.getPostRemove();
        specFile.version = getVersion();
        specFile.buildRoot = packageRoot;
        specFile.writeToFile( specFilePath );

        new Rpmbuild().
            setDebug( debug ).
            setBuildroot( packageRoot ).
            define( "_tmppath " + tmp.getAbsolutePath() ).
            define( "_topdir " + workingDirectory.getAbsolutePath() ).
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
