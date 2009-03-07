package org.codehaus.mojo.unix.maven;

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

import junit.framework.TestCase;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScriptUtilTest
    extends TestCase
{
    private final ScriptUtil scriptUtil;

    {
        scriptUtil = new ScriptUtil.ScriptUtilBuilder().
            format( "pkg" ).
            setPreInstall( "preinstall" ).
            setPostInstall( "postinstall" ).
            setPreRemove( "preremove" ).
            setPostRemove( "postremove" ).
            addCustomScript( "i.daemon" ).
            build();
    }

    public void testWithoutAnyFiles()
        throws IOException
    {
        File basedir = PlexusTestCase.getTestFile( "src/test/resources/script-util/nothing-here" );

        File toDir = PlexusTestCase.getTestFile( "target/script-util/without-any-files" );

        FileUtils.deleteDirectory( toDir );

        scriptUtil.copyScripts( basedir, toDir );

        assertFalse( toDir.exists() );
    }

    public void testWithCommonFilesOnly()
        throws IOException
    {
        File basedir = PlexusTestCase.getTestFile( "src/test/resources/script-util/test-with-common-files-only" );

        File toDir = PlexusTestCase.getTestFile( "target/script-util/test-with-common-files-only" );

        if ( toDir.exists() )
        {
            FileUtils.deleteDirectory( toDir );
        }

        scriptUtil.copyScripts( basedir, toDir );

        assertTrue( toDir.isDirectory() );

        assertEquals( 2, toDir.listFiles().length );

        File preInstall = new File( toDir, "preinstall" );
        assertEquals( 21, preInstall.length() );

        File postRemove = new File( toDir, "postremove" );
        assertEquals( 21, postRemove.length() );
    }

    public void testWithSpecificFilesOnly()
        throws IOException
    {
        File basedir = PlexusTestCase.getTestFile( "src/test/resources/script-util/test-with-specific-files-only" );

        File toDir = PlexusTestCase.getTestFile( "target/script-util/test-with-specific-files-only" );

        if ( toDir.exists() )
        {
            FileUtils.deleteDirectory( toDir );
        }

        scriptUtil.copyScripts( basedir, toDir );

        assertTrue( toDir.isDirectory() );

        assertEquals( 3, toDir.listFiles().length );

        File iDaemon = new File( toDir, "i.daemon" );
        assertEquals( 11, iDaemon.length() );

        File preInstall = new File( toDir, "preinstall" );
        assertEquals( 22, preInstall.length() );

        File postRemove = new File( toDir, "postremove" );
        assertEquals( 22, postRemove.length() );
    }

    public void testWithBoth()
        throws IOException
    {
        File basedir = PlexusTestCase.getTestFile( "src/test/resources/script-util/test-with-both" );

        File toDir = PlexusTestCase.getTestFile( "target/script-util/test-with-both" );

        if ( toDir.exists() )
        {
            FileUtils.deleteDirectory( toDir );
        }

        scriptUtil.copyScripts( basedir, toDir );

        assertTrue( toDir.isDirectory() );

        assertEquals( 3, toDir.listFiles().length );

        File iDaemon = new File( toDir, "i.daemon" );
        assertEquals( 11, iDaemon.length() );

        File preInstall = new File( toDir, "preinstall" );
        assertEquals( 43, preInstall.length() );

        File postRemove = new File( toDir, "postremove" );
        assertEquals( 43, postRemove.length() );
    }
}
