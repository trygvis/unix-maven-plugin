package org.codehaus.mojo.unix;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnixFileModeTest
    extends TestCase
{
    public void testConstants()
    {
        testConstant( UnixFileMode._0644, "rw-r--r--", "644" );
        testConstant( UnixFileMode._0755, "rwxr-xr-x", "755" );
    }

    public void testParsing()
    {
        assertEquals( "0001", UnixFileMode.fromString( "--------x" ).toOctalString() );
        assertEquals( "0544", UnixFileMode.fromString( "r-xr--r--" ).toOctalString() );
        assertEquals( "0755", UnixFileMode.fromString( "rwxr-xr-x" ).toOctalString() );
    }

    private void testConstant( UnixFileMode unixFileMode, String string, String octalString )
    {
        assertEquals( "unixFileMode.toInt()", octalString, Integer.toString( unixFileMode.toInt(), 8 ) );
        assertEquals( "unixFileMode.toString().length()", string.length(), unixFileMode.toString().length() );
        assertEquals( "unixFileMode.toString()", string, unixFileMode.toString() );

        assertEquals( "UnixFileMode.fromString(string).toOctalString()", "0" + octalString, UnixFileMode.fromString( string ).toOctalString() );
    }
}
