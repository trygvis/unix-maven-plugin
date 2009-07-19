package org.codehaus.mojo.unix.pkg;

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

import static fj.data.List.*;
import static fj.data.Option.*;
import junit.framework.*;
import org.codehaus.mojo.unix.util.line.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFileTest
    extends TestCase
{
    public void testParsing()
    {
        LineFile pkginfoStrings = (LineFile) ( new LineFile().
            add( "        PKGINST:  project-pkg-1" ).
            add( "           NAME:  Hudson" ).
            add( "       CATEGORY:  application" ).
            add( "           ARCH:  all" ).
            add( "        VERSION:  1.1-2" ).
            add( "         PSTAMP:  20090129.134909" ).
            add( "          EMAIL:  trygvis@codehaus.org" ).
            add( "         STATUS:  spooled" ).
            add( "          FILES:        3 spooled pathnames" ).
            add( "                        2 package information files" ).
            add( "                    40281 blocks used (approx)" ) );

        PkginfoFile expected = new PkginfoFile();
        expected.packageName = "project-pkg-1";
        expected.name = "Hudson";
        expected.category = "application";
        expected.arch = some( "all" );
        expected.version = "1.1-2";
        expected.pstamp = "20090129.134909";
        expected.email = "trygvis@codehaus.org";
        assertEquals( expected.toString(), PkginfoFile.factory.fromStream( pkginfoStrings ).toString() );
    }

    public void testClasses()
    {
        PkginfoFile pkginfoFile = new PkginfoFile();

        assertEquals( new LineFile().
            add( "PKG=null").
            add( "NAME=").
            add( "DESC=").
            add( "VERSION=null").
            add( "PSTAMP=null").
//            add( "CLASSES=none"). I think this is the right behaviou.
//  If pkgmk always insert *and* warn about the class it should be commented back in
            add( "ARCH=all").
            add( "CATEGORY=application").
            toString(), pkginfoFile.toString() );

        pkginfoFile = new PkginfoFile();
        pkginfoFile.classes = single( "smf" );

        assertEquals( new LineFile().
            add( "PKG=null").
            add( "NAME=").
            add( "DESC=").
            add( "VERSION=null").
            add( "PSTAMP=null").
            add( "CLASSES=smf").
            add( "ARCH=all").
            add( "CATEGORY=application").
            toString(), pkginfoFile.toString() );

        pkginfoFile = new PkginfoFile();
        pkginfoFile.classes = list( "none", "smf" );

        assertEquals( new LineFile().
            add( "PKG=null").
            add( "NAME=").
            add( "DESC=").
            add( "VERSION=null").
            add( "PSTAMP=null").
            add( "CLASSES=none smf").
            add( "ARCH=all").
            add( "CATEGORY=application").
            toString(), pkginfoFile.toString() );
    }
}
