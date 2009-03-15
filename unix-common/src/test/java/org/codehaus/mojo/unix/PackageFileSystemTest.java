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

import fj.F2;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import static fj.data.Option.some;
import junit.framework.TestCase;
import static org.codehaus.mojo.unix.UnixFileMode._0644;
import static org.codehaus.mojo.unix.UnixFileMode._0755;
import static org.codehaus.mojo.unix.UnixFsObject.directory;
import static org.codehaus.mojo.unix.UnixFsObject.regularFile;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.joda.time.LocalDateTime;

import java.util.Comparator;

/**
 * TODO: Assert the creation of parent directories.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageFileSystemTest
    extends TestCase
{
    final LocalDateTime lm = new LocalDateTime( 2009, 2, 24, 9, 42 );

    Option<String> mygroup = some( "mygroup" );

    Option<String> myuser = Option.some( "myuser" );

    FileAttributes fileA = new FileAttributes( myuser, mygroup, some( _0644 ) );

    FileAttributes directoryA = new FileAttributes( myuser, mygroup, some( _0755 ) );

    UnixFsObject.Directory a = directory( fromString( "/a" ), lm, directoryA );

    UnixFsObject.Directory b = directory( fromString( "/b" ), lm, directoryA.mode( UnixFileMode.none ) );

    UnixFsObject.RegularFile a_a = regularFile( fromString( "/a/a" ), lm, 10, some( fileA ) );

    UnixFsObject.RegularFile a_b = regularFile( fromString( "/a/b" ), lm, 10, some( fileA ) );

    UnixFsObject.RegularFile b_a = regularFile( fromString( "/b/a" ), lm, 10, some( fileA ) );

    public void testBasic()
    {
        FileAttributes tjoho = a_a.getFileAttributes().user( "tjoho" );

        PackageFileSystem fileSystem = new PackageFileSystem( comparator );

        fileSystem.addDirectory( b, b );
        fileSystem.addDirectory( a, a );
        fileSystem.addFile( a_a, a_a );
        fileSystem.addFile( a_b, a_b );
        fileSystem.addFile( b_a, b_a );
        fileSystem.apply( filter( "a/", fileA.user( "tjoho" ) ) );

        List actual = iterableList( fileSystem );
        assertEquals( 5, actual.length() );
        int i = 0;
        assertEquals( a, actual.index( i++ ) );
        assertEquals( a_a.setFileAttributes( tjoho ), actual.index( i++ ) );
        assertEquals( a_b.setFileAttributes( tjoho ), actual.index( i++ ) );
        assertEquals( b, actual.index( i++ ) );
        assertEquals( b_a, actual.index( i++ ) );
    }

    Comparator<UnixFsObject> comparator = new Comparator<UnixFsObject>()
    {
        public int compare( UnixFsObject a, UnixFsObject b )
        {
            return a.compareTo( b );
        }
    };

    private F2<UnixFsObject, FileAttributes, FileAttributes> filter( final String s, final FileAttributes newAttributes )
    {
        return new F2<UnixFsObject, FileAttributes, FileAttributes>()
        {
            public FileAttributes f( UnixFsObject fsObject, FileAttributes attributes )
            {
                return !fsObject.path.string.startsWith( s ) ? attributes : attributes.useAsDefaultsFor( newAttributes );
            }
        };
    }
}
