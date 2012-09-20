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
import fj.data.*;
import static fj.data.List.*;
import static java.util.regex.Pattern.*;
import junit.framework.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.core.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import static org.codehaus.mojo.unix.util.line.LineStreamWriter.EOL;

import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

import java.io.*;
import java.nio.charset.*;
import java.util.zip.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ZipPackageTest
    extends TestCase
{
    private final TestUtil testUtil = new TestUtil( getClass() );

    private final Charset charset = Charset.forName( "utf-8" );

    LocalDateTime dirsTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );
    LocalDateTime dirsBarTxtTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );
    LocalDateTime fileTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 48 );
    LocalDateTime fileFooTxtTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );

    // Zip has a resolution of two seconds
    LocalDateTime timestamp = new LocalDateTime( 2012, 1, 2, 3, 4, 6 );

    public void testBasic()
        throws Exception
    {
        File zip1 = testUtil.getTestFile( "src/test/resources/zip/zip-1" );
        File zip = testUtil.getTestFile( "target/zip/zip-1/test.zip" );
        if ( !zip.getParentFile().isDirectory() )
        {
            assertTrue( zip.getParentFile().mkdirs() );
        }

        LocalFs basedir = new LocalFs( zip1 );

        ZipUnixPackage zipPackage = new ZipUnixPackage();

        zipPackage.beforeAssembly( EMPTY, timestamp );

        assertTrue( basedir.isDirectory() );

        // Git set the timestamp of file objects
        assertTrue(new File(zip1, "dirs").setLastModified(dirsTimestamp.toDateTime().getMillis()));
        assertTrue(new File(zip1, "dirs/bar.txt").setLastModified(dirsBarTxtTimestamp.toDateTime().getMillis()));
        assertTrue(new File(zip1, "file").setLastModified(fileTimestamp.toDateTime().getMillis()));
        assertTrue(new File(zip1, "file/foo.txt").setLastModified(fileFooTxtTimestamp.toDateTime().getMillis()));

        Replacer replacer = new Replacer( compile( "@bar@" ), "awesome" );

        new CreateDirectoriesOperation( timestamp, new String[]{ "/opt/hudson" }, EMPTY ).
            perform( zipPackage );

        new CopyDirectoryOperation( basedir, relativePath( "" ), List.<String>nil(), List.<String>nil(),
                                    Option.<P2<String, String>>none(), EMPTY, EMPTY ).
            perform( zipPackage );

        new CopyFileOperation( EMPTY, basedir.resolve( "file/foo.txt" ), relativePath( "/file/foo.txt" ) ).
            perform( zipPackage );

        new FilterFilesOperation( single( "dirs/**" ), List.<String>nil(), single( replacer ), LineEnding.unix ).
            perform( zipPackage );

        zipPackage.
            packageToFile( zip, ScriptUtil.Strategy.SINGLE );

        FileInputStream fis = new FileInputStream( zip );
        ZipInputStream in = new ZipInputStream( fis );
        assertDirectory( in, "./dirs/", dirsTimestamp );
        // Is it really correct that filtered files should retain the old timestamp?
        assertFile( in, "./dirs/bar.txt", 8, dirsBarTxtTimestamp, "awesome\n" );
        assertDirectory( in, "./file/", fileTimestamp );
        assertFile( in, "./file/foo.txt", 6, fileFooTxtTimestamp, "@foo@\n" );
        assertDirectory( in, "./opt/", timestamp );
        assertDirectory( in, "./opt/hudson/", timestamp );
        assertNull( in.getNextEntry() );
        in.close();
        fis.close();
    }

    private void assertDirectory( ZipInputStream in, String name, LocalDateTime time )
        throws IOException
    {
        ZipEntry entry = in.getNextEntry();
        assertNotNull( name, entry );
        assertTrue( name + " should be file", entry.isDirectory() );
        assertEquals( name + ", name", name, entry.getName() );
        assertEquals( name + ", timestamp", time, new LocalDateTime( entry.getTime() ) );
        in.closeEntry();
    }

    private void assertFile( ZipInputStream in, String name, int size, LocalDateTime time, String content )
        throws IOException
    {
        ZipEntry entry = in.getNextEntry();
        assertNotNull( entry );
        assertFalse( name + " should be file", entry.isDirectory() );
        assertEquals( name + ", name", name, entry.getName() );
        assertEquals( name + ", timestamp", time, new LocalDateTime( entry.getTime() ) );
        // wtf: http://vimalathithen.blogspot.no/2006/06/using-zipentrygetsize.html
        // assertEquals( name + ", size", size, entry.getSize() );

        byte[] bytes = new byte[1000];
        assertEquals( size, in.read( bytes, 0, bytes.length ) );
        assertEquals( content, new String( bytes, 0, size, charset ) );
        in.closeEntry();
    }
}
