package org.codehaus.mojo.unix.maven;

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

import static fj.P.*;
import fj.*;
import fj.pre.*;
import static fj.pre.Equal.*;
import junit.framework.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import static org.codehaus.mojo.unix.maven.MojoHelper.*;
import org.codehaus.mojo.unix.maven.plugin.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: MojoHelper.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class MojoHelperDefaultsTest
    extends TestCase
{
    SolarisUnixPlatform platform = new SolarisUnixPlatform();

    FileAttributes platformFileAttributes = platform.getDefaultFileAttributes();

    FileAttributes platformDirectoryAttributes = platform.getDefaultDirectoryAttributes();

    Equal<P2<FileAttributes, FileAttributes>> fileAttributesEqual = p2Equal( Equal.<FileAttributes>anyEqual(),
                                                                             Equal.<FileAttributes>anyEqual() );

    public void testNoDefaults()
    {
        assertTrue( fileAttributesEqual.eq( p( platformFileAttributes, platformDirectoryAttributes ),
                                            calculateDefaultFileAttributes( platform, new Defaults(),
                                                                            new Defaults() ) ) );
    }

    public void testMojoDefaults()
    {
        Defaults mojo = new Defaults( new MojoFileAttributes( "mojo", null, null ), new MojoFileAttributes() );

        assertTrue( fileAttributesEqual.eq( p( platformFileAttributes.user( "mojo" ), platformDirectoryAttributes ),
                                            calculateDefaultFileAttributes( platform, mojo, new Defaults() ) ) );
    }

    public void testPackageDefaults()
    {
        Defaults pakke = new Defaults( new MojoFileAttributes( "package", null, null ), new MojoFileAttributes() );

        assertTrue( fileAttributesEqual.eq( p( platformFileAttributes.user( "package" ), platformDirectoryAttributes ),
                                            calculateDefaultFileAttributes( platform, new Defaults(), pakke ) ) );
    }

    public void testPackageOverrideMojoDefaults()
    {
        Defaults mojo = new Defaults( new MojoFileAttributes( "mojo", null, null ), new MojoFileAttributes() );
        Defaults pakke = new Defaults( new MojoFileAttributes( "package", null, null ), new MojoFileAttributes() );

        assertTrue( fileAttributesEqual.eq( p( platformFileAttributes.user( "package" ), platformDirectoryAttributes ),
                                            calculateDefaultFileAttributes( platform, mojo, pakke ) ) );
    }
}
