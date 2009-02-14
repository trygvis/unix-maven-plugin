package org.codehaus.mojo.unix.util;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RelativePathTest
    extends TestCase
{
    public void testConstructor()
    {
        assertEquals( ".", RelativePath.fromString( "" ).string );
        assertEquals( ".", RelativePath.fromString( null ).string );
        assertEquals( ".", RelativePath.fromString( "/" ).string );
        assertEquals( ".", RelativePath.fromString( "/." ).string );
        assertEquals( ".", RelativePath.fromString( "//" ).string );
        assertEquals( "a", RelativePath.fromString( "a/" ).string );
        assertEquals( "a", RelativePath.fromString( "/a" ).string );
        assertEquals( "a", RelativePath.fromString( "/a/" ).string );

        assertEquals( "a", RelativePath.fromString( "/a//" ).string );

        assertEquals( "a/b", RelativePath.fromString( "/a/b/" ).string );

        assertEquals( "a/b/c", RelativePath.fromString( "/a////b//c////" ).string );

        assertEquals( ".a", RelativePath.fromString( ".a" ).string );
        assertEquals( ".", RelativePath.fromString( "/." ).string );
        assertEquals( "a", RelativePath.fromString( "a/." ).string );
        assertEquals( "a", RelativePath.fromString( "/a/." ).string );

        assertEquals( "opt/jb/.bash_profile", RelativePath.fromString( "/opt/jb/.bash_profile" ).string );
        assertEquals( ".bash_profile", RelativePath.fromString( ".bash_profile" ).string );

        assertEquals( "opt/jetty/bin", RelativePath.fromString( "/opt/jetty/bin" ).string );
        assertEquals( "opt/jetty", RelativePath.fromString( "/opt/jetty/" ).string );
        assertEquals( "opt", RelativePath.fromString( "/opt" ).string );
    }

    public void testAdd()
    {
        assertEquals( ".", RelativePath.fromString( "" ).add( "" ).string );
        assertEquals( ".", RelativePath.fromString( "" ).add( "/" ).string );
        assertEquals( ".", RelativePath.fromString( "/" ).add( "" ).string );
        assertEquals( ".", RelativePath.fromString( "/" ).add( "/" ).string );

        assertEquals( "a", RelativePath.fromString( "./" ).add( "/a/" ).string );
        assertEquals( "a", RelativePath.fromString( "/a/" ).add( "/." ).string );

        assertEquals( "opt/foo/bin/yo", RelativePath.fromString( "/opt/foo" ).add( "/bin/yo" ).string );
        assertEquals( "opt/jetty/README-unix.txt", RelativePath.fromString( "/" ).add( "/opt/jetty/README-unix.txt" ).string );
    }

    public void testAsAbsolutePath()
    {
        assertEquals( "/", RelativePath.fromString( "." ).asAbsolutePath() );
        assertEquals( "/", RelativePath.fromString( "/" ).asAbsolutePath() );
        assertEquals( "/a", RelativePath.fromString( "a" ).asAbsolutePath() );
        assertEquals( "/a", RelativePath.fromString( "a/" ).asAbsolutePath() );
        assertEquals( "/a", RelativePath.fromString( "/a" ).asAbsolutePath() );
        assertEquals( "/a", RelativePath.fromString( "/a/" ).asAbsolutePath() );
        assertEquals( "/a/b", RelativePath.fromString( "a/b" ).asAbsolutePath() );
        assertEquals( "/opt/jb/.bash_profile", RelativePath.fromString( "/opt/jb/.bash_profile" ).asAbsolutePath() );
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
