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

import junit.framework.*;

import java.io.*;

public class FileScannerTest
    extends TestCase
{
    public void testBasic()
        throws IOException
    {
        FileScanner scanner = new FileScanner( new File( "src/test/resources" ), new String[0], new String[0] );

        for ( File file : scanner.toStream() )
        {
            System.out.println( "file = " + file );
        }
    }

    public void testIncludes()
        throws IOException
    {
        File base = new File( System.getProperty( "user.home" ) + "/.m2/repository" );
        FileScanner scanner = new FileScanner( base, new String[]{"**/*.pkg"}, new String[0] );

        for ( File file : scanner.toStream() )
        {
            System.out.println( "file = " + file );
        }
    }

    public void testNoMatch()
        throws IOException
    {
        File base = new File( "src/test/resources" ).getAbsoluteFile();
        FileScanner scanner = new FileScanner( base, new String[]{"**/nothere/**"}, new String[0] );

        assertTrue( scanner.toStream().isEmpty() );
    }
}
