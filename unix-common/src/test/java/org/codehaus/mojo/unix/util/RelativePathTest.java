package org.codehaus.mojo.unix.util;

import junit.framework.TestCase;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RelativePathTest
    extends TestCase
{
    public void testFromString()
    {
        assertEquals( ".", fromString( "" ).string );
        assertEquals( ".", fromString( null ).string );
        assertEquals( ".", fromString( "/" ).string );
        assertEquals( ".", fromString( "/." ).string );
        assertEquals( ".", fromString( "//" ).string );
        assertEquals( "a", fromString( "a/" ).string );
        assertEquals( "a", fromString( "/a" ).string );
        assertEquals( "a", fromString( "/a/" ).string );

        assertEquals( "a", fromString( "/a//" ).string );

        assertEquals( "a/b", fromString( "/a/b/" ).string );

        assertEquals( "a/b/c", fromString( "/a////b//c////" ).string );

        assertEquals( ".a", fromString( ".a" ).string );
        assertEquals( ".", fromString( "/." ).string );
        assertEquals( "a", fromString( "a/." ).string );
        assertEquals( "a", fromString( "/a/." ).string );

        assertEquals( "opt/jb/.bash_profile", fromString( "/opt/jb/.bash_profile" ).string );
        assertEquals( ".bash_profile", fromString( ".bash_profile" ).string );

        assertEquals( "opt/jetty/bin", fromString( "/opt/jetty/bin" ).string );
        assertEquals( "opt/jetty", fromString( "/opt/jetty/" ).string );
        assertEquals( "opt", fromString( "/opt" ).string );
    }

    public void testAdd()
    {
        assertEquals( ".", fromString( "" ).add( "" ).string );
        assertEquals( ".", fromString( "" ).add( "/" ).string );
        assertEquals( ".", fromString( "/" ).add( "" ).string );
        assertEquals( ".", fromString( "/" ).add( "/" ).string );

        assertEquals( "a", fromString( "./" ).add( "/a/" ).string );
        assertEquals( "a", fromString( "/a/" ).add( "/." ).string );

        assertEquals( "opt/foo/bin/yo", fromString( "/opt/foo" ).add( "/bin/yo" ).string );
        assertEquals( "opt/jetty/README-unix.txt", fromString( "/" ).add( "/opt/jetty/README-unix.txt" ).string );
    }

    public void testName()
    {
        assertEquals( "a", fromString( "b/a" ).name() );
        assertEquals( "a", fromString( "a" ).name() );
        assertEquals( ".", fromString( "." ).name() );
    }

    public void testAsAbsolutePath()
    {
        assertEquals( "/", fromString( "." ).asAbsolutePath() );
        assertEquals( "/", fromString( "/" ).asAbsolutePath() );
        assertEquals( "/a", fromString( "a" ).asAbsolutePath() );
        assertEquals( "/a", fromString( "a/" ).asAbsolutePath() );
        assertEquals( "/a", fromString( "/a" ).asAbsolutePath() );
        assertEquals( "/a", fromString( "/a/" ).asAbsolutePath() );
        assertEquals( "/a/b", fromString( "a/b" ).asAbsolutePath() );
        assertEquals( "/opt/jb/.bash_profile", fromString( "/opt/jb/.bash_profile" ).asAbsolutePath() );
    }

    public void testClean()
    {
        assertNull( RelativePath.clean( "" ) );
        assertEquals( "opt/.bash_profile", RelativePath.clean( "opt/.bash_profile" ) );
        assertEquals( "opt/.bash_profile", RelativePath.clean( "opt/.bash_profile" ) );

        assertEquals( "file", RelativePath.clean( "file" ) );
        assertEquals( "file", RelativePath.clean( "/file" ) );
        assertEquals( "file", RelativePath.clean( "//file" ) );
        assertEquals( "file", RelativePath.clean( "./file" ) );

        assertEquals( ".bash_profile", RelativePath.clean( ".bash_profile" ) );
        assertEquals( ".bash_profile", RelativePath.clean( "/.bash_profile" ) );
    }
}
