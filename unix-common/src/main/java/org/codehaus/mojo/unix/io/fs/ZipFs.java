package org.codehaus.mojo.unix.io.fs;

import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

import java.io.*;
import java.util.zip.*;

public class ZipFs
    implements Fs<ZipFs>
{
    public final ZipFsRoot root;

    public final ZipEntry entry;

    public final RelativePath relativePath;

    public ZipFs( ZipFsRoot root, ZipEntry entry, RelativePath relativePath )
    {
        this.root = root;
        this.entry = entry;
        this.relativePath = relativePath;
    }

    public void close()
        throws IOException
    {
        root.close();
    }

    public boolean exists()
    {
        return entry != null;
    }

    public boolean isFile()
    {
        return exists() && !entry.isDirectory();
    }

    public boolean isDirectory()
    {
        return exists() && entry.isDirectory();
    }

    public LocalDateTime lastModified()
    {
        return new LocalDateTime( entry.getTime() );
    }

    public long size()
    {
        return entry.getSize();
    }

    public ZipFs resolve( RelativePath relativePath )
    {
        return root.resolve( this.relativePath.add( relativePath ) );
    }

    public File basedir()
    {
        return root.file;
    }

    public RelativePath relativePath()
    {
        return relativePath;
    }

    public String absolutePath()
    {
        return root.absolutePath( relativePath );
    }

    public InputStream inputStream()
        throws IOException
    {
        return root.zipFile.getInputStream( entry );
    }

    public Iterable<ZipFs> find( IncludeExcludeFilter filter )
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void mkdir()
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void copyFrom( Fs<?> from )
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void copyFrom( Fs from, InputStream is )
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }
}
