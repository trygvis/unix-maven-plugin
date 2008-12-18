package org.codehaus.mojo.unix.dpkg.cli;

import junit.framework.TestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DpkgDebCliTest
    extends TestCase
{
    public void testContents()
        throws Exception
    {
        StringWriter writer = new StringWriter();
        DpkgDebCli cli = new DpkgDebCli( new PrintWriter( writer ) );

        cli.contents( new File( "src/test/resources/base-files_4_i386.deb" ) );

        String expected = FileUtils.fileRead( new File( "src/test/resources/base-files_4_i386.txt" ) );

//        assertEquals(expected, writer.toString());
    }
}
