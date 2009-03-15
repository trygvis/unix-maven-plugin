package org.codehaus.mojo.unix.util;

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
        assertEquals( "/", fromString( "." ).asAbsolutePath( "/" ) );
        assertEquals( "/", fromString( "/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", fromString( "a" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", fromString( "a/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", fromString( "/a" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", fromString( "/a/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a/b", fromString( "a/b" ).asAbsolutePath( "/" ) );
        assertEquals( "/opt/jb/.bash_profile", fromString( "/opt/jb/.bash_profile" ).asAbsolutePath( "/" ) );
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
