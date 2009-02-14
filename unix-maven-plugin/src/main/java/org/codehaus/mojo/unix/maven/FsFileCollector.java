package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.Selectors;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FsFileCollector
    implements FileCollector
{
    private interface Operation
    {
        public void process()
            throws FileSystemException;
    }

    private class PackageFile
        implements Operation
    {
        public final FileObject source;

        public final RelativePath toPath;

        public PackageFile( FileObject source, RelativePath toPath )
        {
            this.source = source;
            this.toPath = toPath;
        }

        public void process()
            throws FileSystemException
        {
            FileObject toFile = root.resolveFile( toPath.string );

            System.out.println( "Copying from " + source.getName().getPath() + " to " + toFile.getName().getPath() );

            toFile.getParent().createFolder();
            toFile.copyFrom( source, Selectors.SELECT_SELF );
            toFile.getContent().setLastModifiedTime( source.getContent().getLastModifiedTime() );
        }
    }

    private class PackageDirectory
        implements Operation
    {
        public final RelativePath toPath;

        public PackageDirectory( RelativePath toPath )
        {
            this.toPath = toPath;
        }

        public void process()
            throws FileSystemException
        {
            root.resolveFile( toPath.string ).createFolder();
        }
    }

    private List files = new ArrayList();

    private boolean debug;

    private FileObject fsRoot;

    private FileObject root;

    public FsFileCollector( FileObject fsRoot )
        throws FileSystemException
    {
        this.fsRoot = fsRoot;
        FileSystemManager fileSystemManager = fsRoot.getFileSystem().getFileSystemManager();
        FileObject root = fileSystemManager.createVirtualFileSystem( fsRoot );
        root.createFolder();
        this.root = root;
    }

    public FileObject getFsRoot()
    {
        return fsRoot;
    }

    public FileObject getRoot()
    {
        return root;
    }

    public FileCollector addDirectory( RelativePath path, FileAttributes attributes )
    {
        files.add( new PackageDirectory( path ) );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, RelativePath toPath, FileAttributes attributes )
    {
        files.add( new PackageFile( fromFile, toPath ) );

        return this;
    }

    public void debug( boolean flag )
    {
        this.debug = flag;
    }

    public void collect()
        throws IOException
    {
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            ((Operation) it.next()).process();
        }
    }
}
