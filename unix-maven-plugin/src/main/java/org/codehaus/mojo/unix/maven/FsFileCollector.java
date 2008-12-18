package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FsFileCollector
    implements FileCollector
{
    private static class PackageFile
    {
        public final FileObject source;

        public final String toFile;

        public PackageFile( FileObject source, String toFile )
        {
            this.source = source;
            this.toFile = toFile;
        }
    }

    private static class PackageDirectory
    {
        public final String path;

        public PackageDirectory(String path)
        {
            this.path = path;
        }
    }

    private List files = new ArrayList();

    private boolean debug;

    public FileCollector addDirectory( String path, String user, String group, String mode )
        throws IOException
    {
        files.add( new PackageDirectory( path ) );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
    {
        files.add( new PackageFile( fromFile, toFile ) );

        return this;
    }

    public void debug( boolean flag )
    {
        this.debug = flag;
    }

    public void collect( File basedir )
        throws IOException
    {
        if ( basedir == null )
        {
            throw new IOException( "The package assembly directory is not set." );
        }

        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            Object o = it.next();

            if ( o instanceof PackageFile)
            {
                processFile( basedir, (PackageFile) o );
            }
            else if ( o instanceof PackageDirectory )
            {
                processDirectory( basedir, ((PackageDirectory) o).path );
            }
        }
    }

    private void processFile( File basedir, PackageFile packageFile )
        throws IOException
    {
        File to = new File( basedir, packageFile.toFile );

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
            IOUtil.copy( packageFile.source.getContent().getInputStream(), output );
        }
        finally
        {
            IOUtil.close( output );
        }

        to.setLastModified( packageFile.source.getContent().getLastModifiedTime() );
    }

    private void processDirectory( File basedir, String path )
        throws IOException
    {
        File to = new File( basedir, path );

        if ( !to.exists() )
        {
            if ( debug )
            {
                System.out.println( "Creating directory: " + to );
            }

            if ( !to.mkdirs() )
            {
                throw new IOException( "Unable to create directory: " + to.getAbsolutePath() );
            }
        }
    }
}
