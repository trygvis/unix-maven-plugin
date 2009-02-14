package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
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
public class Extract
    extends AssemblyOperation
{
    private String archive;

    private String artifact;

    private RelativePath toDir = RelativePath.BASE;

    private List includes;

    private List excludes;

    private String pattern;

    private String replacement;

    private FileAttributes fileAttributes = new FileAttributes();

//    private org.codehaus.mojo.unix.FileAttributes unixFileAttributes;

    private FileAttributes directoryAttributes = new FileAttributes();

//    private org.codehaus.mojo.unix.FileAttributes unixDirectoryAttributes;

    public Extract()
    {
        super( "extract" );
    }

    public void setArchive( String archive )
    {
        this.archive = nullifEmpty( archive );
    }

    public void setArtifact( String artifact )
    {
        this.artifact = nullifEmpty( artifact );
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

    public void setPattern( String pattern )
    {
        this.pattern = nullifEmpty( pattern );
    }

    public void setReplacement( String replacement )
    {
        this.replacement = nullifEmpty( replacement );
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
        validate();

        FileSystemManager fsManager = VFS.getManager();
        FileObject archiveObject = fsManager.resolveFile( getTheFile().getAbsolutePath() );
        FileObject archive = fsManager.createFileSystem( archiveObject );

        org.codehaus.mojo.unix.FileAttributes fileAttributes =
            Defaults.DEFAULT_FILE_ATTRIBUTES.
                useAsDefaultsFor( defaults.getFileAttributes() ).
                    useAsDefaultsFor( this.fileAttributes.create() );

        org.codehaus.mojo.unix.FileAttributes directoryAttributes =
            Defaults.DEFAULT_DIRECTORY_ATTRIBUTES.
                useAsDefaultsFor( defaults.getDirectoryAttributes() ).
                    useAsDefaultsFor( this.directoryAttributes.create() );

        copyFiles( fileCollector, archive, toDir, includes, excludes, pattern, replacement,
            fileAttributes, directoryAttributes );
    }

    private File getTheFile()
        throws MojoFailureException
    {
        File file;

        if ( this.archive != null )
        {
            file = new File( this.archive );
        }
        else
        {
            file = artifact( this.artifact ).getFile();
        }
        return file;
    }

    private void validate()
        throws MojoFailureException
    {
        validateEitherIsSet( archive, artifact, "archive", "artifact" );

        if ( pattern != null && replacement == null )
        {
            throw new MojoFailureException( "A replacement expression has to be set if a pattern is given." );
        }
    }
}
