package org.codehaus.mojo.unix.ar;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArWriterTest
    extends TestCase
{
    private static File yo = new File( System.getProperty( "basedir" ), "target/yo.deb" );
    private static File yo2 = new File( System.getProperty( "basedir" ), "target/yo2.deb" );
    private static File hello = new File( System.getProperty( "basedir" ), "target/hello.txt" );

    public static void writeFiles()
        throws Exception
    {
        if ( yo.exists() )
        {
            assertTrue( yo.delete() );
        }
        if ( yo2.exists() )
        {
            assertTrue( yo2.delete() );
        }
        if ( hello.exists() )
        {
            assertTrue( hello.delete() );
        }

        Writer writer = new OutputStreamWriter( new FileOutputStream( hello ), "UTF-16" );
        writer.write( "Hello World!" );
        writer.close();
    }

    public ArWriterTest()
        throws Exception
    {
        writeFiles();
    }

    public void test1()
        throws Exception
    {
        ArWriter writer = new ArWriter( yo );
        writer.close();

        assertTrue( yo.canRead() );
        assertEquals( 8, yo.length() );
    }

    public void test2()
        throws Exception
    {
        ArWriter writer = new ArWriter( yo );
        writer.close();

        ArFile yoFile = new ArFile();
        yoFile.file = yo;
        yoFile.name = yo.getName();

        ArFile helloFile = new ArFile();
        helloFile.file = hello;
        helloFile.name = hello.getName();

        writer = new ArWriter( yo2 );
        writer.add( yoFile );
        writer.add( helloFile );
        writer.close();

        assertTrue( yo2.canRead() );
        int expectedSize =
            8 +      // ar magic
                60 + 8 + // entry header + data
                60 + 26;
        assertEquals( expectedSize, yo2.length() );
    }
}
