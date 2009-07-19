package org.codehaus.mojo.unix.sysvpkg.prototype;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import fj.data.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;
import static java.lang.Boolean.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FileEntry
    extends PrototypeEntry<RegularFile>
{
    private final Option<File> realPath;

    /**
     * The same as calling {@link #FileEntry(Option, Option, UnixFsObject.RegularFile, Option)}
     * with <code>relative=false</code> and <code>realPath=null</code>.
     */
    public FileEntry( Option<String> pkgClass, RegularFile file )
    {
        this( pkgClass, some( FALSE ), file, Option.<File>none() );
    }

    public FileEntry( Option<String> pkgClass, Option<Boolean> relative, RegularFile file, Option<File> realPath )
    {
        super( pkgClass, relative, file );
        this.realPath = realPath;
    }

    public String generatePrototypeLine()
    {
        return "f " + pkgClass + " " + getProcessedPath( realPath ) + " " + toString( object.getFileAttributes() );
    }

    public FileAttributes getFileAttributes()
    {
        return object.getFileAttributes();
    }

    public FileEntry setFileAttributes( FileAttributes attributes )
    {
        return new FileEntry( some( pkgClass ), relative, object.setFileAttributes( attributes ), realPath );
    }

    public FileEntry setPath( RelativePath path )
    {
        return new FileEntry( some( pkgClass ), relative, object.setPath( path ), realPath );
    }
}
