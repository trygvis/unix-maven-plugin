package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Copy
    extends AssemblyOperation
{
    private File path;

    private String artifact;

    private String toFile;

    private RelativePath toDir = RelativePath.BASE;

    private List includes;

    private List excludes;

    private FileAttributes fileAttributes = new FileAttributes();

    private FileAttributes directoryAttributes = new FileAttributes();

    public Copy()
    {
        super( "copy" );
    }

    public void setPath( File path )
    {
        this.path = path;
    }

    public void setArtifact( String artifact )
    {
        this.artifact = nullifEmpty( artifact );
    }

    public void setToFile( String toFile )
    {
        this.toFile = toFile;
    }

    public void setToDir( String toDir )
    {
        this.toDir = RelativePath.fromString( toDir );
    }

    public void setIncludes( List includes )
    {
        this.includes = includes;
    }

    public void setExcludes( List excludes )
    {
        this.excludes = excludes;
    }

    public void setFileAttributes( FileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public void setDirectoryAttributes( FileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }

    public void perform( FileObject basedir, Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException
    {
        validateEitherIsSet( path, artifact, "path", "artifact" );

        org.codehaus.mojo.unix.FileAttributes unixFileAttributes =
            Defaults.DEFAULT_FILE_ATTRIBUTES.
                useAsDefaultsFor( defaults.getFileAttributes() ).
                    useAsDefaultsFor( fileAttributes.create() );

        org.codehaus.mojo.unix.FileAttributes unixDirectoryAttributes =
            Defaults.DEFAULT_DIRECTORY_ATTRIBUTES.
                useAsDefaultsFor( defaults.getDirectoryAttributes() ).
                    useAsDefaultsFor( directoryAttributes.create() );

        if ( path != null )
        {
            FileObject fromFile = basedir.resolveFile( path.getAbsolutePath() );

            if ( fromFile.getType() == FileType.FILE )
            {
                copyFile( fileCollector, fromFile, toFile, toDir, unixFileAttributes );
            }
            else if ( fromFile.getType() == FileType.FOLDER )
            {
                copyFiles( fileCollector, fromFile, toDir, includes, excludes, null, null,
                    unixFileAttributes, unixDirectoryAttributes );
            }
        }
        else
        {
            File artifactFile = artifact( this.artifact ).getFile();

            // This code fails on windows, seems like commons-vfs is unable to properly resolve the path.
            // FileObject artifact = basedir.resolveFile( artifactFile.getAbsolutePath() );
            FileObject artifact = basedir.getFileSystem().getRoot().resolveFile( artifactFile.getAbsolutePath() );

            copyFile( fileCollector, artifact, toFile, toDir, unixFileAttributes );
        }
    }

    private void copyFile( FileCollector fileCollector, FileObject fromFile, String toFile, RelativePath toDir,
                           org.codehaus.mojo.unix.FileAttributes unixFileAttributes )
        throws IOException
    {
        if ( !fromFile.isReadable() )
        {
            throw new RuntimeException( "File is not readable: " + fromFile.getName().getPath() );
        }

        String relativeName;

        if ( toFile != null )
        {
            relativeName = toFile;
        }
        else
        {
            relativeName = fromFile.getName().getBaseName();
        }

        fileCollector.addFile( fromFile, toDir.add( relativeName ), unixFileAttributes );
    }
}
