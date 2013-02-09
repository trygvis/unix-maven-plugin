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
import fj.data.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class FsFileCollector
    implements FileCollector
{
    private final List<IoEffect> operations = new ArrayList<IoEffect>();

    public final LocalFs root;

    public FsFileCollector( LocalFs root) throws IOException {
        this.root = root;
        mkdirs(root.file);
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        operations.add( packageDirectory( directory.path ) );
    }

    public void addFile( Fs fromFile, UnixFsObject.RegularFile file )
    {
        operations.add( packageFile( fromFile, file ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        operations.add( packageSymlink( symlink ) );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        // Not implemented
    }

    public void collect()
        throws Exception
    {
        for ( IoEffect operation : operations )
        {
            operation.run();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private IoEffect packageFile( final Fs from, final UnixFsObject.RegularFile to )
    {
        return new IoEffect()
        {
            public void run()
                throws Exception
            {
                root.resolve( to.path ).copyFrom( from );
            }
        };
    }

    private IoEffect packageDirectory( final RelativePath path )
    {
        return new IoEffect()
        {
            public void run()
                throws Exception
            {
                mkdirs( root.resolve( path ).file );
            }
        };
    }

    private IoEffect packageSymlink( final UnixFsObject.Symlink symlink )
    {
        return new IoEffect()
        {
            public void run()
                throws Exception
            {
                mkdirs( root.resolve( symlink.path ).file.getParentFile() );

                UnixUtil.symlink( root.file, symlink.value, symlink.path );
            }
        };
    }

    private void mkdirs( File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return;
        }

        if ( !file.mkdirs() )
        {
            throw new IOException( "Unable to create root directory: " + root.file.getAbsolutePath() );
        }
    }
}
