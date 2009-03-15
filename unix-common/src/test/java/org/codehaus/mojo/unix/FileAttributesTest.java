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

import static fj.Function.compose;
import static fj.Function.curry;
import static fj.data.Option.fromNull;
import junit.framework.TestCase;
import static org.codehaus.mojo.unix.FileAttributes.useAsDefaultsFor;
import static org.codehaus.mojo.unix.FileAttributes.EMPTY;
import static org.codehaus.mojo.unix.UnixFileMode.none;
import static org.codehaus.mojo.unix.util.UnixUtil.noneString;

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

    public FileAttributes test( String defaultDefaults, String defaults, String attributes )
    {
        return compose( curry( useAsDefaultsFor, new FileAttributes( fromNull( defaultDefaults ), noneString, none ) ),
                        curry( useAsDefaultsFor, new FileAttributes( fromNull( defaults ), noneString, none ) ) ).
            f( new FileAttributes( fromNull( attributes ), noneString, none ) );
    }
}
