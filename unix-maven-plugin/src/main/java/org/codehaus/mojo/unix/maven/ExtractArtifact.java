package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExtractArtifact
    extends AbstractFileSetOp
{
    private String artifact;

    public ExtractArtifact()
    {
        super( "extract-artifact" );
    }

    public void setArtifact( String artifact )
    {
        this.artifact = nullifEmpty( artifact );
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        File artifactFile = validateArtifact( artifact );

        FileSystemManager fsManager = VFS.getManager();
        FileObject archiveObject = fsManager.resolveFile( artifactFile.getAbsolutePath() );
        FileObject archive = fsManager.createFileSystem( archiveObject );

        return createOperationInternal( archive, defaults );
    }
}
