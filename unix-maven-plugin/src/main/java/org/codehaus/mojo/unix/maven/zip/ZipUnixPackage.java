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

/**
 */
public class ZipUnixPackage
    extends UnixPackage
{
    private PackageFileSystem<F2<UnixFsObject, ZipOutputStream, Callable>> fileSystem;

    private FileObject workingDirectory;

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

    public FileObject getRoot()
    {
        return workingDirectory;
    }

    public FileCollector addDirectory( final UnixFsObject.Directory directory )
        throws IOException
    {
        BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>> o =
            new BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>>( directory, this.directory );

        fileSystem = fileSystem.addDirectory( o );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
        throws IOException
    {
        F2<UnixFsObject, ZipOutputStream, Callable> f = uncurryF2( curry( this.file, fromFile ) );

        BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>> o =
            new BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>>( file, f );

        fileSystem = fileSystem.addFile( o );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        return this;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    // -----------------------------------------------------------------------
    // UnixPackage Implementation
    // -----------------------------------------------------------------------

    public ZipUnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes )
        throws IOException
    {
        Directory rootDirectory = directory( BASE, new LocalDateTime( System.currentTimeMillis() ), EMPTY );

        BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>> fileSystemObject =
            new BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>>( rootDirectory, directory );
        fileSystem = create( fileSystemObject, fileSystemObject );
    }

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        F2<RelativePath, PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>>, Boolean> pathFilter =
            ZipUnixPackage.pathFilter();

        List<PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>>> items = fileSystem.
            toList().
            filter( compose( BooleanF.invert, curry( pathFilter, BASE ) ) );

        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream( new FileOutputStream( packageFile ) );

            for ( PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, Callable>> fileSystemObject : items )
            {
                fileSystemObject.getExtension().f( fileSystemObject.getUnixFsObject(), zos ).call();
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

    private final F2<UnixFsObject, ZipOutputStream, Callable> directory =
        new F2<UnixFsObject, ZipOutputStream, Callable>()
        {
            public Callable f( final UnixFsObject unixFsObject, final ZipOutputStream zipOutputStream )
            {
                return new Callable()
                {
                    public Object call()
                        throws Exception
                    {
                        String path = unixFsObject.path.isBase() ? "." : unixFsObject.path.asAbsolutePath( "./" ) + "/";

                        zipOutputStream.putNextEntry( new ZipEntry( path ) );

                        return null;
                    }
                };
            }
        };

    private final F3<FileObject, UnixFsObject, ZipOutputStream, Callable> file =
        new F3<FileObject, UnixFsObject, ZipOutputStream, Callable>()
        {
            public Callable f( final FileObject fromFile, final UnixFsObject unixFsObject,
                               final ZipOutputStream zipOutputStream )
            {
                return new Callable()
                {
                    public Object call()
                        throws Exception
                    {
                        RegularFile file = (RegularFile) unixFsObject;

                        ZipEntry zipEntry = new ZipEntry( unixFsObject.path.asAbsolutePath( "./" ) );
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
}
