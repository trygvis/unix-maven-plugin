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
import static org.codehaus.mojo.unix.util.UnixUtil.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SymlinkEntry
    extends PrototypeEntry<Symlink>
{
    protected SymlinkEntry( Option<String> pkgClass, Symlink symlink )
    {
        super( pkgClass, Option.<Boolean>none(), symlink );
    }

    public String generatePrototypeLine()
    {
        return "s " + pkgClass + " " + getPath() + "=" + object.value;
    }

    public FileAttributes getFileAttributes()
    {
        return object.getFileAttributes();
    }

    public SymlinkEntry setFileAttributes( FileAttributes attributes )
    {
        return new SymlinkEntry( some( pkgClass ), object.setFileAttributes( attributes ) );
    }

    public PackageFileSystemObject<PrototypeEntry> setPath( RelativePath path )
    {
        return new SymlinkEntry( Option.some( pkgClass ), object.setPath( path ) );
    }
}
