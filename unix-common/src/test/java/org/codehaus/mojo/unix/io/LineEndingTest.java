package org.codehaus.mojo.unix.io;

import junit.framework.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

public class LineEndingTest
    extends TestCase
{
    public void testDetect()
        throws Exception
    {
        assertResult( new byte[]{ 'a', 'b', '\n'}, LineEnding.unix );
        assertResult( new byte[]{ 'a', 'b', '\n', 'c', 'd'}, LineEnding.unix );
        assertResult( new byte[]{ 'a', 'b', '\r', '\n', 'c', 'd'}, LineEnding.windows );
    }

    private void assertResult( byte[] bytes, LineEnding lineEnding )
        throws IOException
    {
        Map.Entry<InputStream, LineEnding> x = LineEnding.detect( new ByteArrayInputStream( bytes ) );

        assertEquals( lineEnding, x.getValue() );
        byte[] actualBytes = IOUtil.toByteArray( x.getKey() );
        assertEquals( bytes.length, actualBytes.length );
        for ( int i = 0; i < actualBytes.length; i++ )
        {
            assertEquals( "byte #" + i, bytes[i], actualBytes[i] );
        }
    }
}
