package org.codehaus.mojo.unix.io.fs;

import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

import java.io.*;

/**
 * Move the write methods into WrFs&lt;F extends Fs> extends Fs&lt;F>
 */
public interface Fs<F extends Fs>
    extends Closeable
{
    boolean exists();

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
        throws IOException;

    Iterable<F> find( IncludeExcludeFilter filter )
        throws IOException;

    void mkdir()
        throws IOException;

    void copyFrom( Fs<?> from )
        throws IOException;

    void copyFrom( Fs from, InputStream is )
        throws IOException;
}
