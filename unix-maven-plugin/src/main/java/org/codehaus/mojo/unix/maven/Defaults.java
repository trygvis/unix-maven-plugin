package org.codehaus.mojo.unix.maven;

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

import static fj.data.Option.some;
import static org.codehaus.mojo.unix.UnixFileMode._0644;
import static org.codehaus.mojo.unix.UnixFileMode._0755;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Defaults
{
    public final static org.codehaus.mojo.unix.FileAttributes DEFAULT_FILE_ATTRIBUTES =
        new org.codehaus.mojo.unix.FileAttributes( some( "nobody" ), some( "nogroup" ), some( _0644 ) );

    public final static org.codehaus.mojo.unix.FileAttributes DEFAULT_DIRECTORY_ATTRIBUTES =
        new org.codehaus.mojo.unix.FileAttributes( some( "nobody" ), some( "nogroup" ), some( _0755 ) );

    private FileAttributes fileAttributes = new FileAttributes();

    private FileAttributes directoryAttributes = new FileAttributes();

    public org.codehaus.mojo.unix.FileAttributes getFileAttributes()
    {
        return fileAttributes.create();
    }

    public void setFile( FileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public org.codehaus.mojo.unix.FileAttributes getDirectoryAttributes()
    {
        return directoryAttributes.create();
    }

    public void setDirectoryAttributes( FileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }
}
