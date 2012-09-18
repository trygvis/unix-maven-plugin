package org.codehaus.mojo.unix.io.fs;

import org.codehaus.mojo.unix.io.IncludeExcludeFilter;
import org.codehaus.mojo.unix.util.RelativePath;
import org.joda.time.LocalDateTime;

import java.io.*;

public interface Fs<F extends Fs>
{
    boolean isFile();

    boolean isDirectory();

    LocalDateTime lastModified();

    long size();

    F resolve( RelativePath relativePath );

    /**
     * The logical root of this file system. For local file system, it is the File it was created from, for archive
     * file system types it's the archive's File path.
     */
    File basedir();

    /**
     * The path inside the file system.
     */
    RelativePath relativePath();

    /**
     * For local files, File.getAbsolutePath. For archive files it's the archives path + "!" + relativePath
     */
    String absolutePath();

    InputStream inputStream()
        throws FileNotFoundException;

    Iterable<F> find( IncludeExcludeFilter filter );

    void mkdir()
        throws IOException;

    void copyFrom( Fs from )
        throws IOException;
}
