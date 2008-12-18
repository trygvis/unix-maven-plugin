package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileType;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Extract
    extends AssemblyOperation
{
    private String archive;

    private String artifact;

    private String toDir;

    private String includes;

    private String excludes;

    private String pattern;

    private String replacement;

    private String fileUser;

    private String fileGroup;

    private String fileMode;

    private String directoryUser;

    private String directoryGroup;

    private String directoryMode;

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
        this.toDir = nullifEmpty( toDir );
    }

    public void setIncludes( String includes )
    {
        this.includes = nullifEmpty( includes );
    }

    public void setExcludes( String excludes )
    {
        this.excludes = nullifEmpty( excludes );
    }

    public void setPattern( String pattern )
    {
        this.pattern = nullifEmpty( pattern );
    }

    public void setReplacement( String replacement )
    {
        this.replacement = nullifEmpty( replacement );
    }

    public void setFileUser( String fileUser )
    {
        this.fileUser = fileUser;
    }

    public void setFileGroup( String fileGroup )
    {
        this.fileGroup = fileGroup;
    }

    public void setFileMode( String fileMode )
    {
        this.fileMode = fileMode;
    }

    public void setDirectoryUser( String directoryUser )
    {
        this.directoryUser = directoryUser;
    }

    public void setDirectoryGroup( String directoryGroup )
    {
        this.directoryGroup = directoryGroup;
    }

    public void setDirectoryMode( String directoryMode )
    {
        this.directoryMode = directoryMode;
    }

    public void perform( Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException
    {
        validate();

        fileUser = StringUtils.isNotEmpty( fileUser ) ? fileUser : defaults.getFileUser();
        fileGroup = StringUtils.isNotEmpty( fileGroup ) ? fileGroup : defaults.getFileGroup();
        fileMode = StringUtils.isNotEmpty( fileMode ) ? fileMode : defaults.getFileMode();

        directoryUser = StringUtils.isNotEmpty( directoryUser ) ? directoryUser : defaults.getDirectoryUser();
        directoryGroup = StringUtils.isNotEmpty( directoryGroup ) ? directoryGroup : defaults.getDirectoryGroup();
        directoryMode = StringUtils.isNotEmpty( directoryMode ) ? directoryMode : defaults.getDirectoryMode();

        File file;

        if ( this.archive != null )
        {
            file = new File( this.archive );
        }
        else
        {
            file = artifact( this.artifact ).getFile();
        }

        FileSystemManager fsManager = VFS.getManager();
        FileObject archiveObject = fsManager.resolveFile( file.getAbsolutePath() );
        FileObject archive = fsManager.createFileSystem( archiveObject );

        if ( pattern != null )
        {
            extractPatternBased( fileCollector, archive );
        }
        else
        {
            extract( fileCollector, archive );
        }
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

    private void extractPatternBased( final FileCollector fileCollector, final FileObject archive )
        throws FileSystemException
    {
        final Pattern pattern = Pattern.compile( this.pattern );

        archive.findFiles( new FileSelector()
        {
            public boolean includeFile( FileSelectInfo fileSelectInfo )
                throws Exception
            {
                String name = "/" + archive.getName().getRelativeName( fileSelectInfo.getFile().getName() );

                Matcher matcher = pattern.matcher( name );

                String transformed = matcher.replaceAll( replacement );

                if ( transformed.equals( name ) )
                {
                    return false;
                }

                // TODO: Log this
                FileObject fileObject = fileSelectInfo.getFile();

                if ( fileObject.getType() == FileType.FILE )
                {
                    fileCollector.addFile( fileObject, toDir + "/" + transformed, fileUser, fileGroup, fileMode );
                }
                else if ( fileObject.getType() == FileType.FOLDER )
                {
                    fileCollector.addDirectory( toDir + "/" + transformed, directoryUser, directoryGroup, directoryMode );
                }

                return false;
            }

            public boolean traverseDescendents( FileSelectInfo fileSelectInfo )
                throws Exception
            {
                return true;
            }
        } );
    }

    private void extract( final FileCollector fileCollector, final FileObject archive )
        throws FileSystemException
    {
        // TODO: implement includes/excludes
        archive.findFiles( new FileSelector()
        {
            public boolean includeFile( FileSelectInfo fileSelectInfo )
                throws Exception
            {
                String name = "/" + archive.getName().getRelativeName( fileSelectInfo.getFile().getName() );

                // TODO: Log this
                FileObject fileObject = fileSelectInfo.getFile();

                if ( fileObject.getType() == FileType.FILE )
                {
                    fileCollector.addFile( fileObject, toDir + "/" + name, fileUser, fileGroup, fileMode );
                }
                else if ( fileObject.getType() == FileType.FOLDER )
                {
                    fileCollector.addDirectory( toDir + "/" + name, directoryUser, directoryGroup, directoryMode );
                }

                return false;
            }

            public boolean traverseDescendents( FileSelectInfo fileSelectInfo )
                throws Exception
            {
                return true;
            }
        } );
    }
}
