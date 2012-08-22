package org.codehaus.mojo.unix.maven.zip;

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
import static fj.Function.*;
import fj.data.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.PackageFileSystem.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.java.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;

import java.io.*;
import java.util.concurrent.*;
import java.util.zip.*;

public class ZipUnixPackage
    extends UnixPackage
{
    private PackageFileSystem<F<ZipOutputStream, Callable>> fileSystem;

    public ZipUnixPackage()
    {
        super( "zip" );
    }

    public UnixPackage parameters( PackageParameters parameters )
    {
        return this;
    }

    // -----------------------------------------------------------------------
    // FileCollector Implementation
    // -----------------------------------------------------------------------

    public void addDirectory( final Directory directory )
        throws IOException
    {
        fileSystem = fileSystem.addDirectory( directory(directory ) );
    }

    public void addFile( FileObject fromFile, RegularFile file )
        throws IOException
    {
        fileSystem = fileSystem.addFile( file( fromFile, file ) );
    }

    public void addSymlink( Symlink symlink )
        throws IOException
    {
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    // -----------------------------------------------------------------------
    // UnixPackage Implementation
    // -----------------------------------------------------------------------

    public ZipUnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes )
        throws IOException
    {
        Directory rootDirectory = Directory.directory( BASE, new LocalDateTime( System.currentTimeMillis() ), EMPTY );

        fileSystem = create( directory(rootDirectory), directory(rootDirectory) );
    }

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        F2<RelativePath, PackageFileSystemObject<F<ZipOutputStream, Callable>>, Boolean> pathFilter =
            ZipUnixPackage.pathFilter();

        Stream<PackageFileSystemObject<F<ZipOutputStream, Callable>>> items = fileSystem.
            toList().
            filter( compose( BooleanF.invert, curry( pathFilter, BASE ) ) );

        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream( new FileOutputStream( packageFile ) );

            for ( PackageFileSystemObject<F<ZipOutputStream, Callable>> fileSystemObject : items )
            {
                fileSystemObject.getExtension().f( zos ).call();
            }
        }
        finally
        {
            IOUtil.close( zos );
        }
    }

    public static <A> F2<RelativePath, PackageFileSystemObject<A>, Boolean> pathFilter()
    {
        return new F2<RelativePath, PackageFileSystemObject<A>, Boolean>()
        {
            public Boolean f( RelativePath path, PackageFileSystemObject object )
            {
                return object.getUnixFsObject().path.equals( path );
            }
        };
    }

    private BasicPackageFileSystemObject<F<ZipOutputStream, Callable>> directory( final Directory directory )
    {
        F<ZipOutputStream, Callable> f = new F<ZipOutputStream, Callable>()
        {
            public Callable f( final ZipOutputStream zipOutputStream )
            {
                return new Callable()
                {
                    public Object call()
                        throws Exception
                    {
                        String path = directory.path.isBase() ? "." : directory.path.asAbsolutePath( "./" ) + "/";

                        zipOutputStream.putNextEntry( new ZipEntry( path ) );

                        return null;
                    }
                };
            }
        };

        return new BasicPackageFileSystemObject<F<ZipOutputStream, Callable>>( directory, f );
    }

    private BasicPackageFileSystemObject<F<ZipOutputStream, Callable>> file( final FileObject fromFile,
                                                                             final RegularFile file )
    {
        F<ZipOutputStream, Callable> f = new F<ZipOutputStream, Callable>()
        {
            public Callable f( final ZipOutputStream zipOutputStream )
            {
                return new Callable()
                {
                    public Object call()
                        throws Exception
                    {
                        ZipEntry zipEntry = new ZipEntry( file.path.asAbsolutePath( "./" ) );
                        zipEntry.setSize( file.size );
                        zipEntry.setTime( file.lastModified.toDateTime().getMillis() );
                        zipOutputStream.putNextEntry( zipEntry );

                        InputStream inputStream = null;
                        try
                        {
                            inputStream = fromFile.getContent().getInputStream();
                            IOUtil.copy( inputStream, zipOutputStream, 1024 * 128 );
                        }
                        finally
                        {
                            IOUtil.close( inputStream );
                        }

                        return null;
                    }
                };
            }
        };
        return new BasicPackageFileSystemObject<F<ZipOutputStream, Callable>>( file, f );
    }
}
