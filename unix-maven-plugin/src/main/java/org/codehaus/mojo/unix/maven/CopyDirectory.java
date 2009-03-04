package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import static org.apache.commons.vfs.VFS.getManager;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyDirectory
    extends AbstractFileSetOp
{
    private File from;

    public CopyDirectory()
    {
        super( "copy-directory" );
    }

    public void setFrom( File from )
    {
        this.from = from;
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        File file = validateFileIsDirectory( from, "from" );

        return createOperationInternal( getManager().resolveFile( file.getAbsolutePath() ), defaults );
    }
}
