package org.codehaus.mojo.unix.core;

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

import fj.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FsFileCollector
    implements FileCollector
{
    private final List<Callable> operations = new ArrayList<Callable>();

    private final FileObject fsRoot;

    private final FileObject root;

    public FsFileCollector( FileObject fsRoot )
        throws FileSystemException
    {
        this.fsRoot = fsRoot;
        FileSystemManager fileSystemManager = fsRoot.getFileSystem().getFileSystemManager();
        FileObject root = fileSystemManager.createVirtualFileSystem( fsRoot );
        root.createFolder();
        this.root = root;
    }

    public FileObject getFsRoot()
    {
        return fsRoot;
    }

    public FileObject getRoot()
    {
        return root;
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
    {
        operations.add( packageDirectory( directory.path ) );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        operations.add( packageFile( fromFile, file ) );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        operations.add( packageSymlink( symlink ) );

        return this;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        // Not implemented
    }

    public void collect()
        throws Exception
    {
        for ( Callable operation : operations )
        {
            operation.call();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private Callable packageFile( final FileObject from, final UnixFsObject.RegularFile to )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                FileObject toFile = root.resolveFile( to.path.string );

                toFile.getParent().createFolder();
                toFile.copyFrom( from, Selectors.SELECT_SELF );
                toFile.getContent().setLastModifiedTime( to.lastModified.toDateTime().toDate().getTime() );
                return Unit.unit();
            }
        };
    }

    private Callable packageDirectory( final RelativePath path )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                root.resolveFile( path.string ).createFolder();
                return Unit.unit();
            }
        };
    }

    private Callable packageSymlink( final UnixFsObject.Symlink symlink )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                root.resolveFile( symlink.path.string ).getParent().createFolder();

                File file = asFile( fsRoot );

                UnixUtil.symlink( file, symlink.value, symlink.path );

                return Unit.unit();
            }
        };
    }
}
