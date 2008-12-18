package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FilesystemAssemblyService
    implements AssemblyService
{
    private File basedir;

    public FilesystemAssemblyService( File basedir )
    {
        this.basedir = basedir;
    }

    // -----------------------------------------------------------------------
    // AssemblyService Implementation
    // -----------------------------------------------------------------------

    public void copyFile( FileObject from, String toPath )
        throws IOException
    {
        File to = new File( basedir, toPath );

        File parentFile = to.getParentFile();

        if ( !parentFile.isDirectory() )
        {
            if ( !parentFile.mkdirs() )
            {
                throw new IOException( "Could not create directory: '" + parentFile.getAbsolutePath() + "'." );
            }
        }

        // TODO: Figure out how to use VFS to do this
        OutputStream output = null;

        try
        {
            output = new FileOutputStream( to );
            IOUtil.copy( from.getContent().getInputStream(), output );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------
}
