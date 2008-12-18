package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Copy
    extends AssemblyOperation
{
    private File path;

    private String artifact;

    private String toFile;

    private String toDir;

    private String fileUser;

    private String fileGroup;

    private String fileMode;

    private String directoryUser;

    private String directoryGroup;

    private String directoryMode;

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
        this.toDir = toDir;
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
        validateEitherIsSet( path, artifact, "path", "artifact" );

        fileUser = StringUtils.isNotEmpty( fileUser ) ? fileUser : defaults.getFileUser();
        fileGroup = StringUtils.isNotEmpty( fileGroup ) ? fileGroup : defaults.getFileGroup();
        fileMode = StringUtils.isNotEmpty( fileMode ) ? fileMode : defaults.getFileMode();

        directoryUser = StringUtils.isNotEmpty( directoryUser ) ? directoryUser : defaults.getDirectoryUser();
        directoryGroup = StringUtils.isNotEmpty( directoryGroup ) ? directoryGroup : defaults.getDirectoryGroup();
        directoryMode = StringUtils.isNotEmpty( directoryMode ) ? directoryMode : defaults.getDirectoryMode();

        if ( path != null )
        {
            FileObject fromFile = getFsManager().resolveFile( path.getAbsolutePath() );

            if ( fromFile.getType() == FileType.FILE )
            {
                addFile( fileCollector, fromFile, adjustToFile( toFile, toDir, path.getName() ) );
            }
            else if ( fromFile.getType() == FileType.FOLDER )
            {
                if ( StringUtils.isEmpty( toDir ) )
                {
                    throw new IOException( "toDir has to be specified when copying a directory." );
                }

                copyDirectory( fileCollector, fromFile, toDir );
            }
        }
        else
        {
            Artifact a = artifact( artifact );

            addFile( fileCollector, getFsManager().resolveFile( a.getFile().getAbsolutePath() ),
                adjustToFile( toFile, toDir, a.getFile().getName() ));
        }
    }

    private static String adjustToFile( String toFile, String toDir, String path )
        throws IOException
    {
        if ( toFile != null )
        {
            return toFile;
        }

        if ( StringUtils.isEmpty( toDir ) )
        {
            throw new IOException( "toDir has to be set when toFile is empty" );
        }

        return toDir + "/" + path;
    }

    private void addFile( FileCollector fileCollector, FileObject fromFile, String toFile )
        throws IOException
    {
        if ( !fromFile.isReadable() )
        {
            throw new RuntimeException( "File is not readable: " + fromFile.getName().getPath() );
        }

        fileCollector.addFile( fromFile, toFile, fileUser, fileGroup, fileMode );
    }

    private void copyDirectory( FileCollector fileCollector, FileObject fromFile, String toFile )
        throws IOException
    {
        System.out.println( "Copy.copyDirectory" );

        System.out.println( "Directory = " + toFile );
        fileCollector.addDirectory( toFile, directoryUser, directoryGroup, directoryMode );

        FileObject[] children = fromFile.getChildren();

        for ( int i = 0; i < children.length; i++ )
        {
            FileObject child = children[i];

            String toPath = toFile + "/" + child.getName().getBaseName();

            if ( child.getType() == FileType.FOLDER )
            {
                System.out.println( "Directory = " + toPath );
                copyDirectory( fileCollector, child, toPath );
            }
            else if ( child.getType() == FileType.FILE )
            {
                System.out.println( "File = " + toPath );
                fileCollector.addFile( child, toPath, fileUser, fileGroup, fileMode );
            }
        }
    }
}
