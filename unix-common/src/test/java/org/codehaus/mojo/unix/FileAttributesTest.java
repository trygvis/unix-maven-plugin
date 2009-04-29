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
import static org.codehaus.mojo.unix.FileAttributes.*;

public class FileAttributesTest
    extends TestCase
{
    public void testEquals()
    {
        assertEquals( EMPTY, EMPTY );

        assertFalse( EMPTY.user( "trygve" ).equals( EMPTY ) );
    }

    public void testUseAsDefaultsForComposition()
    {
        assertEquals( EMPTY.user( "c" ), test( "a", "b", "c" ) );
        assertEquals( EMPTY.user( "c" ), test( "a", null, "c" ) );
        assertEquals( EMPTY.user( "c" ), test( null, "b", "c" ) );

        assertEquals( EMPTY.user( "b" ), test( "a", "b", null ) );
        assertEquals( EMPTY.user( "a" ), test( "a", null, null ) );
        assertEquals( EMPTY.user( "b" ), test( null, "b", null ) );
    }

    public FileAttributes test( String defaultDefaultUser, String defaultUser, String user )
    {
        return EMPTY.user( defaultDefaultUser ).
            useAsDefaultsFor( EMPTY.user( defaultUser ) ).
            useAsDefaultsFor( EMPTY.user( user ) );
    }
}
