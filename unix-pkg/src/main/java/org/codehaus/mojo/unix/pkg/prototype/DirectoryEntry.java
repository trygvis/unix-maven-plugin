package org.codehaus.mojo.unix.pkg.prototype;

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

import fj.data.Option;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.util.RelativePath;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DirectoryEntry
    extends PrototypeEntry
{
    private final FileAttributes attributes;

    /**
     * The same as calling {@link #DirectoryEntry(Option, RelativePath, Option, FileAttributes)}
     * with <code>relative=none()</code>.
     */
    public DirectoryEntry( Option<String> pkgClass, RelativePath path, FileAttributes attributes )
    {
        this( pkgClass, path, Option.<Boolean>none(), attributes );
    }

    public DirectoryEntry( Option<String> pkgClass, RelativePath path, Option<Boolean> relative, FileAttributes attributes )
    {
        super( pkgClass, relative, path );
        this.attributes = attributes;
    }

    public String generatePrototypeLine()
    {
        return "d " + pkgClass +
            " " + getPath() +
            " " + toString( attributes );
    }

    public FileAttributes getFileAttributes()
    {
        return attributes;
    }

    public DirectoryEntry setFileAttributes( FileAttributes attributes )
    {
        return new DirectoryEntry( some( pkgClass ), path, relative, attributes );
    }
}
