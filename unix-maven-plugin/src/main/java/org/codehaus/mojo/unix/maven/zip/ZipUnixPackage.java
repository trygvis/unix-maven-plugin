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
import static fj.Show.*;
import fj.data.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.BasicPackageFileSystemObject.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.PackageFileSystem.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.UnixFsObject.Replacer.*;
import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.java.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;

import java.io.*;
import java.util.zip.*;

public class ZipUnixPackage
    extends UnixPackage
{
    private PackageFileSystem<F2<UnixFsObject, ZipOutputStream, IoEffect>> fileSystem;

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
        fileSystem = fileSystem.addDirectory( directory( directory ) );
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

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException
    {
        Directory rootDirectory = Directory.directory( BASE, timestamp, EMPTY );

        fileSystem = create( directory( rootDirectory ), directory( rootDirectory ) );
    }

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        F2<RelativePath, PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, IoEffect>>, Boolean> pathFilter =
            pathFilter();

        PackageFileSystemFormatter<F2<UnixFsObject, ZipOutputStream, IoEffect>> formatter =
            PackageFileSystemFormatter.flatFormatter();

        fileSystem = fileSystem.prettify();

        Stream<PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, IoEffect>>> items = fileSystem.
            toList().
            filter( compose( BooleanF.invert, curry( pathFilter, BASE ) ) );

        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream( new FileOutputStream( packageFile ) );

            for ( PackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, IoEffect>> fileSystemObject : items )
            {
                fileSystemObject.getExtension().f( fileSystemObject.getUnixFsObject(), zos ).run();
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

    private BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, IoEffect>> directory( Directory directory )
    {
        F2<UnixFsObject, ZipOutputStream, IoEffect> f = new F2<UnixFsObject, ZipOutputStream, IoEffect>()
        {
            public IoEffect f( final UnixFsObject object, final ZipOutputStream zipOutputStream )
            {
                return new IoEffect()
                {
                    public void run()
                        throws Exception
                    {
                        String path = object.path.isBase() ? "." : object.path.asAbsolutePath( "./" ) + "/";

                        ZipEntry entry = new ZipEntry( path );
                        entry.setTime( object.lastModified.toDateTime().getMillis() );
                        zipOutputStream.putNextEntry( entry );
                    }
                };
            }
        };

        return basicPackageFSO( directory, f );
    }

    private BasicPackageFileSystemObject<F2<UnixFsObject, ZipOutputStream, IoEffect>> file( final FileObject fromFile,
                                                                                            UnixFsObject file )
    {
        F2<UnixFsObject, ZipOutputStream, IoEffect> f = new F2<UnixFsObject, ZipOutputStream, IoEffect>()
        {
            public IoEffect f( final UnixFsObject file, final ZipOutputStream zipOutputStream )
            {
                return new IoEffect()
                {
                    public void run()
                        throws Exception
                    {
                        InputStream inputStream = null;
                        BufferedReader reader = null;
                        try
                        {
                            @SuppressWarnings( "unchecked" ) List<Replacer> filters = file.filters;

                            long size;

                            inputStream = fromFile.getContent().getInputStream();

                            if ( filters.isEmpty() )
                            {
                                size = file.size;
                            }
                            else
                            {
                                // TODO: Ideally create an output stream that doesn't create a new array on
                                // toByteArray, but instead can be used as an InputStream directly.
                                // TODO: This is going to add additional LF/CRLF at the end of the file. Fuck it.
                                ByteArrayOutputStream output = new ByteArrayOutputStream( (int) file.size );
                                PrintWriter writer = new PrintWriter( output );

                                // This implicitly uses the platform encoding. Not smart.
                                reader = new BufferedReader( new InputStreamReader( inputStream ), 1024 * 128 );

                                String line = reader.readLine();

                                while ( line != null )
                                {
                                    for ( Replacer filter : filters )
                                    {
                                        line = filter.replace( line );
                                    }
                                    writer.println( line );

                                    line = reader.readLine();
                                }
                                inputStream.close();
                                writer.flush();

                                inputStream = new ByteArrayInputStream( output.toByteArray() );
                                size = output.size();
                            }

                            ZipEntry zipEntry = new ZipEntry( file.path.asAbsolutePath( "./" ) );
                            zipEntry.setSize( size );
                            zipEntry.setTime( file.lastModified.toDateTime().getMillis() );
                            zipOutputStream.putNextEntry( zipEntry );
                            IOUtil.copy( inputStream, zipOutputStream, 1024 * 128 );
                        }
                        finally
                        {
                            IOUtil.close( inputStream );
                            IOUtil.close( reader );
                        }
                    }
                };
            }
        };

        return basicPackageFSO( file, f );
    }
}
