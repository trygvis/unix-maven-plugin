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

import junit.framework.*;
import org.apache.commons.vfs.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.core.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;

/**
 */
public class ZipPackageTest
    extends TestCase
{
    public void testBasic()
        throws Exception
    {
        FileSystemManager fileSystemManager = VFS.getManager();

        File zip1 = new TestUtil( this ).getTestFile( "src/it/test-zip-1" );
        File zip = new File( zip1, "target/zip/test.zip" );
        if ( !zip.getParentFile().isDirectory() )
        {
            assertTrue( zip.getParentFile().mkdirs() );
        }

        FileObject basedir = fileSystemManager.resolveFile( zip1.getAbsolutePath() );

        ZipUnixPackage zipPackage = new ZipUnixPackage();

        zipPackage.beforeAssembly( EMPTY );

        assertEquals( FileType.FOLDER, basedir.getType() );

        new CreateDirectoriesOperation( new String[]{"/opt/hudson"}, EMPTY ).
            perform( zipPackage );

        new CopyFileOperation( EMPTY, basedir.resolveFile( "pom4test.xml" ), relativePath( "/opt/hudson/hudson.war" ) ).
            perform( zipPackage );

        zipPackage.
            packageToFile( zip, ScriptUtil.Strategy.SINGLE );
    }
}
