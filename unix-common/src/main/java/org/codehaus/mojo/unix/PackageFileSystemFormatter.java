package org.codehaus.mojo.unix;

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
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;

public class PackageFileSystemFormatter<A>
{
    /*
    TODO: Create strategies for printing in "flat", "flat with detailes" and "tree" modes

    flat:
    .
    a
    b
    c
    c/c-x
    c/c-x/c-x-r

    flat with details:
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 .
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 a
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 b
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c/c-x
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c/c-x/c-x-r

    tree:
    .
    |-- a
    |-- b
    `-- c
        `-- c-x
            `-- c-x-r
    */

    private PackageFileSystemFormatter()
    {
    }

    public static <A> PackageFileSystemFormatter<A> flatFormatter()
    {
        return new PackageFileSystemFormatter<A>();
    }

    // TODO: This should be streaming instead of creating an entire collection
    public LineFile print( PackageFileSystem<A> fs )
    {
        LineFile lines = new LineFile();

        Tree<PackageFileSystemObject<A>> tree = fs.prettify().getTree();

        lines.add( "." );

        print( 1, lines, tree.subForest() );

        return lines;
    }

    private void print( int i, LineFile lines, List<Tree<PackageFileSystemObject<A>>> children )
    {
        String indent = StringUtils.repeat( "    ", i );
        i++;

        for ( Tree<PackageFileSystemObject<A>> child : children )
        {
            lines.add( indent + child.root().getUnixFsObject().path.name() );
            print( i, lines, child.subForest() );
        }
    }
}
