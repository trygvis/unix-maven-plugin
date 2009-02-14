package org.codehaus.mojo.unix.maven;

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
