package org.codehaus.mojo.unix.sysvpkg;

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
import fj.data.*;
import junit.framework.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.sysvpkg.PkginfoFile.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFileTest
    extends TestCase
{
    public void testParsing()
    {
        Iterable<String> pkginfoStrings = (LineFile) ( new LineFile().
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

        PkginfoFile expected = new PkginfoFile( "all", "application", "Hudson", "project-pkg-1", "1.1-2",
                                                some( "20090129.134909" ), Option.<String>none(),
                                                some( "trygvis@codehaus.org" ), List.<String>nil() );

        assertEquals( expected.toString(), fromStream( pkginfoStrings ).some().toString() );
    }

    public void testClasses()
    {
        PkginfoFile pkginfoFile = new PkginfoFile( "all", "application", "name", "mypackage", "1.0" );

        assertEquals( new LineFile().
            add( "ARCH=all").
            add( "CATEGORY=application").
            add( "NAME=name").
            add( "PKG=mypackage").
            add( "VERSION=1.0").
//            add( "CLASSES=none"). I think this is the right behaviou.
//  If pkgmk always insert *and* warn about the class it should be commented back in
            toString(), pkginfoFile.toString() );

        pkginfoFile = pkginfoFile.
            classes( list( "smf" ) );

        assertEquals( new LineFile().
            add( "ARCH=all").
            add( "CATEGORY=application").
            add( "NAME=name").
            add( "PKG=mypackage").
            add( "VERSION=1.0").
            add( "CLASSES=smf").
            toString(), pkginfoFile.toString() );

        pkginfoFile = pkginfoFile.
            classes( list( "none", "smf" ) );

        assertEquals( new LineFile().
            add( "ARCH=all").
            add( "CATEGORY=application").
            add( "NAME=name").
            add( "PKG=mypackage").
            add( "VERSION=1.0").
            add( "CLASSES=none smf").
            toString(), pkginfoFile.toString() );
    }
}
