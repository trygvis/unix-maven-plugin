package org.codehaus.mojo.unix.maven.pkg.prototype;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.local.LocalFileSystem;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFile
{
    private final List includeFiles = new ArrayList();

    private final PrototypeEntryList entryList = new PrototypeEntryList();

    private final List tmpFiles = new ArrayList();

    private FileObject root;

    public PrototypeFile( FileObject root )
    {
        this.root = root;
    }

    public void includeFileIf( File file, String name )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        includeFiles.add( "i " + name + "=" + file.getAbsolutePath() );
    }

    public boolean hasPath( String path )
    {
        return entryList.hasPath( path );
    }

    public void addDirectory( RelativePath path, FileAttributes attributes )
        throws FileSystemException
    {
        entryList.add( new DirectoryEntry( null, attributes.mode, attributes.user, attributes.group, path ) );
    }

    public void addFile( FileObject fromFile, RelativePath toFile, FileAttributes attributes )
        throws IOException
    {
        try
        {
            File realPath;

            // If it is a file on the local file system, just point the entry in the prototype file to it
            if ( fromFile.getFileSystem() instanceof LocalFileSystem )
            {
                realPath = VfsUtil.asFile( fromFile ).getCanonicalFile();
            }
            else
            {
                // Extract the entry
                realPath = File.createTempFile( "unix-plugin-", null );
                realPath.deleteOnExit();
                tmpFiles.add( realPath );
                OutputStream outputStream = null;
                try
                {
                    outputStream = new FileOutputStream( realPath );
                    IOUtil.copy( fromFile.getContent().getInputStream(), outputStream );
                    if ( !realPath.setLastModified( fromFile.getContent().getLastModifiedTime() ) )
                    {
                        throw new IOException( "Unable to set last modified on " + realPath.getAbsolutePath() );
                    }
                }
                finally
                {
                    IOUtil.close( outputStream );
                }
            }

            entryList.add( new FileEntry( null,
                attributes.mode, attributes.user, attributes.group, toFile, Boolean.FALSE, realPath ) );

            // TODO: add missing parent directory entries
        }
        catch ( FileSystemException e )
        {
            IOException ex = new IOException( "Error while adding file." );
            ex.initCause( e );
            throw ex;
        }
    }

    public LineFile toLineFile()
        throws IOException
    {
        LineFile file = new LineFile();

        file.addAllLines( includeFiles );

        for ( Iterator it = entryList.iterator(); it.hasNext(); )
        {
            file.add( ( (SinglePrototypeEntry) it.next() ).getPrototypeLine() );
        }

        return file;
    }

    public void cleanUp()
    {
        for ( Iterator it = tmpFiles.iterator(); it.hasNext(); )
        {
            File file = (File) it.next();

            // Ignore the return value as there is nothing we can do. The files are already marked as "delete on exit"
            // so the JVM should try again too.
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public FileObject getRoot()
    {
        return root;
    }

    private class PrototypeEntryList
    {
        private TreeMap collectedPrototypeEntries;

        private PrototypeEntryList()
        {
            collectedPrototypeEntries = new TreeMap();
        }

        public void add( SinglePrototypeEntry entry )
        {
            collectedPrototypeEntries.remove( entry.getPath() );
            collectedPrototypeEntries.put( entry.getPath(), entry );
        }

        public void addAll( PrototypeEntryList list )
        {
            for ( Iterator it = list.iterator(); it.hasNext(); )
            {
                SinglePrototypeEntry entry = (SinglePrototypeEntry) it.next();

                collectedPrototypeEntries.remove( entry.getPath() );
                collectedPrototypeEntries.put( entry.getPath(), entry );
            }
        }

        public boolean hasPath( String path )
        {
            return collectedPrototypeEntries.containsKey( path );
        }

        public int size()
        {
            return collectedPrototypeEntries.size();
        }

        public Iterator iterator()
        {
            return collectedPrototypeEntries.values().iterator();
        }
    }
}
