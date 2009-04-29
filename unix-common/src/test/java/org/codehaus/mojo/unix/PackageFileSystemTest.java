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

import fj.*;
import fj.data.List;
import fj.data.*;
import static fj.data.Option.*;
import fj.pre.*;
import junit.framework.*;
import static org.codehaus.mojo.unix.PackageFileSystem.*;
import static org.codehaus.mojo.unix.UnixFileMode.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.line.*;
import org.joda.time.*;

import java.util.*;

/**
 * TODO: Assert the creation of parent directories.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageFileSystemTest
    extends TestCase
{
    static LocalDateTime lm = new LocalDateTime( 2009, 2, 24, 9, 42 );

    static Option<String> mygroup = some( "mygroup" );

    static Option<String> myuser = Option.some( "myuser" );

    static FileAttributes fileA = new FileAttributes( myuser, mygroup, some( _0644 ) );

    static FileAttributes directoryA = new FileAttributes( myuser, mygroup, some( _0755 ) );

    static PlainPackageFileSystemObject root = plain( directory( RelativePath.BASE, lm, directoryA ) );

    static PlainPackageFileSystemObject a = plain( directory( relativePath( "/a" ), lm, directoryA ) );

    static PlainPackageFileSystemObject b = plain( directory( relativePath( "/b" ), lm, directoryA.mode( UnixFileMode.none ) ) );

    static PlainPackageFileSystemObject a_x = plain( regularFile( relativePath( "/a/a-x" ), lm, 10, some( fileA ) ) );

    static PlainPackageFileSystemObject a_y = plain( regularFile( relativePath( "/a/a-y" ), lm, 10, some( fileA ) ) );

    static PlainPackageFileSystemObject b_x = plain( regularFile( relativePath( "/b/b-x" ), lm, 10, some( fileA ) ) );

    static PlainPackageFileSystemObject c = plain( directory( relativePath( "/c" ), lm, directoryA ) );

    static PlainPackageFileSystemObject c_x = plain( directory( relativePath( "/c/c-x" ), lm, directoryA ) );

    static PlainPackageFileSystemObject c_x_u = plain( regularFile( relativePath( "/c/c-x/c-x-u" ), lm, 10, some( fileA ) ) );

    Show<List<PackageFileSystemObject<Object>>> fsShow = Show.listShow( Show.showS( new F<PackageFileSystemObject<Object>, String>()
    {
        public String f( PackageFileSystemObject o )
        {
            return o.getUnixFsObject().toString() + "\n";
        }
    } ) );

    public void testAddSingleNodeToRoot()
    {
        PackageFileSystem<Object> fileSystem = PackageFileSystem.<Object>create( root, root ).
            addDirectory( a );

        assertEquals( new LineFile().
            add( "." ).
            add( "    a" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );

        fsShow.println( fileSystem.toList() );
    }

    public void testAddingNestedDirectoryWithParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
//            addDirectory( c ).
            addDirectory( c_x );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedDirectoryWithoutParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( c_x );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedDirectoryWithOnlyOneRelatedParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( c ).
            addFile( c_x_u );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            add( "            c-x-u" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedFileWithUnrelatedParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( b ).
            addFile(c_x_u );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    b" ).
            add( "    c" ).
            add( "        c-x" ).
            add( "            c-x-u" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testBasic()
    {
        FileAttributes tjoho = a_x.getUnixFsObject().getFileAttributes().user( "tjoho" );

        PackageFileSystem<Object> fileSystem = PackageFileSystem.<Object>create( root, root ).
            addDirectory( b ).
            addDirectory( a ).
            addFile( a_y ).
            addFile( a_x ).
            addFile( b_x ).
            addFile( c_x_u ).
            apply( filter( "a/", fileA.user( "tjoho" ) ) );

        System.out.println( "-----------------------------------" );
        System.out.print( PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ) );
        System.out.println( "-----------------------------------" );

        fsShow.println( fileSystem.toList() );

        List<PackageFileSystemObject<Object>> actual = fileSystem.prettify().toList();
        assertEquals( 9, actual.length() );
        int i = 0;
        assertEquals( root.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a_x.getUnixFsObject().setFileAttributes( tjoho ), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a_y.getUnixFsObject().setFileAttributes( tjoho ), actual.index( i++ ).getUnixFsObject() );
        assertEquals( b.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( b_x.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c_x.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c_x_u.getUnixFsObject(), actual.index( i ).getUnixFsObject() );
    }

    public void testMutatingRootNode()
    {
        PackageFileSystem<Object> fs = create( root, root );

        assertEquals( root.getUnixFsObject(), fs.getObject( RelativePath.BASE ).some().getUnixFsObject() );

        FileAttributes newAttributes = root.getUnixFsObject().getFileAttributes().user( "woot" );

        fs = fs.addDirectory( plain( directory( RelativePath.BASE, lm, newAttributes ) ) );

        assertEquals( newAttributes, fs.getObject( RelativePath.BASE ).some().getUnixFsObject().getFileAttributes() );
    }

    public static final Comparator<UnixFsObject> comparator = new Comparator<UnixFsObject>()
    {
        public int compare( UnixFsObject a, UnixFsObject b )
        {
            return a.compareTo( b );
        }
    };

    private F2<UnixFsObject, FileAttributes, FileAttributes> filter( final String s,
                                                                     final FileAttributes newAttributes )
    {
        return new F2<UnixFsObject, FileAttributes, FileAttributes>()
        {
            public FileAttributes f( UnixFsObject fsObject, FileAttributes attributes )
            {
                return !fsObject.path.string.startsWith( s )
                    ? attributes
                    : attributes.useAsDefaultsFor( newAttributes );
            }
        };
    }

    public static PlainPackageFileSystemObject plain( UnixFsObject unixFsObject )
    {
        return new PlainPackageFileSystemObject( unixFsObject );
    }
}
