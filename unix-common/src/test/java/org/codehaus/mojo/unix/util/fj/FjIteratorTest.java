package org.codehaus.mojo.unix.util.fj;

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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import junit.framework.*;

public class FjIteratorTest
    extends TestCase
{
    public void testFiltering()
    {
        List<String> strings = List.list( "t", "f", "t", "f" );

        assertEquals( 4, iterableList( FjIterator.<String>iterator( strings ).toIterable() ).length() );

        List<String> actualStrings = iterableList( FjIterator.iterator( strings ).filter( filterF ).toIterable() );

        assertEquals( 2, actualStrings.length() );
        assertEquals( "t", actualStrings.index( 0 ) );
        assertEquals( "t", actualStrings.index( 1 ) );
    }

    F<String, Boolean> filterF = new F<String, Boolean>()
    {
        public Boolean f( String s )
        {
            return s.endsWith( "t" );
        }
    };
}
