package org.codehaus.mojo.unix;

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
import static org.codehaus.mojo.unix.PackageFileSystem.*;
import static org.codehaus.mojo.unix.PackageFileSystemTest.*;
import org.codehaus.mojo.unix.util.line.*;

public class PackageFileSystemFormatterTest
    extends TestCase
{
    public void testBasic()
    {
        PackageFileSystemFormatter<Object> formatter = PackageFileSystemFormatter.flatFormatter();

        LineFile fs1 = formatter.print( create( root, root ).
            addDirectory( a ).
            addDirectory( b ) );

        assertEquals( new LineFile().add( "." ).add( "    a" ).add( "    b" ), fs1 );

        LineFile fs2 = formatter.print( create( root, root ).
            addDirectory( a ).
            addDirectory( b ).
            addFile( a_x ) );

        assertEquals( new LineFile().
            add( "." ).
            add( "    a" ).
            add( "        a-x" ).
            add( "    b" ), fs2 );

        LineFile fs3 = formatter.print( create( root, root ).
            addDirectory( b ).
            addDirectory( a ).
            addFile( a_x ).
            addFile( b_x ).
            addFile( a_y ).
            addFile( c_x_u ) );

//        System.out.print( formatter.print( fs3 ) );

        assertEquals( new LineFile().
            add(".").
            add("    a").
            add("        a-x").
            add("        a-y").
            add("    b").
            add("        b-x").
            add("    c").
            add("        c-x").
            add("            c-x-u"), fs3 );
    }
}
