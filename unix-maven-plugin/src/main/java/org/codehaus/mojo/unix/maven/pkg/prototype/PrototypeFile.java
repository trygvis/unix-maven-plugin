package org.codehaus.mojo.unix.maven.pkg.prototype;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.local.LocalFileSystem;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFile
{
    private List includeFiles = new ArrayList();

    private PrototypeEntryList entryList = new PrototypeEntryList();

    public void includeFileIf( File file, String name )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        includeFiles.add( "i " + name + "=" + file.getAbsolutePath());
    }

    public void addDirectory( String path, String user, String group, String mode )
    {
        DirectoryEntry entry = new DirectoryEntry();
        entry.setPath( path );
        entry.setUser( user );
        entry.setGroup( group );
        entry.setMode( mode );
        entryList.add( entry );
    }

    public void addFile( FileObject fromFile, String toFile, String user, String group, String mode )
        throws IOException
    {
        try
        {
            FileEntry entry = new FileEntry();
            entry.setPath( toFile );
            entry.setUser( user );
            entry.setGroup( group );
            entry.setMode( mode );

            // If it is a file on the local file system, just point the entry in the prototype file to it
            if ( fromFile.getFileSystem() instanceof LocalFileSystem )
            {
                entry.setRealPath( new File( fromFile.getName().getPath() ).getCanonicalFile() );
            }
            else
            {
                // Extract the entry
                File tmp = File.createTempFile( "unix-plugin-", null );
                OutputStream outputStream = null;
                try
                {
                    outputStream = new FileOutputStream( tmp );
                    IOUtil.copy( fromFile.getContent().getInputStream(), outputStream );
                }
                finally
                {
                    IOUtil.close( outputStream );
                }
                entry.setRealPath( tmp );
            }

            entryList.add( entry );

            // TODO: add missing directory entries
        }
        catch ( FileSystemException e )
        {
            IOException ex = new IOException( "Error while adding file." );
            ex.initCause( e );
            throw ex;
        }
    }

    public void writeTo( File prototype )
        throws IOException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( prototype );
            PrintWriter printer = new PrintWriter( writer );

            for ( Iterator it = includeFiles.iterator(); it.hasNext(); )
            {
                printer.println( it.next().toString() );
            }
//            if ( pkginfoFile != null )
//            {
//                printer.println( "i pkginfo=" + pkginfoFile.getAbsolutePath() );
//            }

            for ( Iterator it = entryList.iterator(); it.hasNext(); )
            {
                printer.println( ( (SinglePrototypeEntry) it.next() ).getPrototypeLine() );
            }
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    private class PrototypeEntryList
    {
        private TreeSet collectedPrototypeEntries;

        private PrototypeEntryList()
        {
            collectedPrototypeEntries = new TreeSet( new Comparator()
            {
                public int compare( Object o, Object o1 )
                {
                    SinglePrototypeEntry a = (SinglePrototypeEntry) o;
                    SinglePrototypeEntry b = (SinglePrototypeEntry) o1;
                    return a.getPath().compareTo( b.getPath() );
                }
            } );
        }

        public void add( SinglePrototypeEntry entry )
        {
            collectedPrototypeEntries.remove( entry );
            collectedPrototypeEntries.add( entry );
        }

        public void addAll( PrototypeEntryList list )
        {
            for ( Iterator it = list.iterator(); it.hasNext(); )
            {
                SinglePrototypeEntry entry = (SinglePrototypeEntry) it.next();

                collectedPrototypeEntries.remove( entry );
                collectedPrototypeEntries.add( entry );
            }
        }

        public int size()
        {
            return collectedPrototypeEntries.size();
        }

        public Iterator iterator()
        {
            return collectedPrototypeEntries.iterator();
        }
    }
}
