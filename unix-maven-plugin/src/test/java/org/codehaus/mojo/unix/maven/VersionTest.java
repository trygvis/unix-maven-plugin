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

import fj.*;
import fj.data.*;
import static fj.data.Option.*;
import junit.framework.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.PackageVersion.*;
import static org.codehaus.mojo.unix.maven.deb.DebUnixPackage.*;
import org.codehaus.mojo.unix.maven.rpm.*;
import static org.codehaus.mojo.unix.maven.sysvpkg.PkgUnixPackage.*;

/**
 * This test is here and not in unix-common because it depend on the actual implementations.
 *
 * The purpose is to assert that the version parsing/generation is similar across all formats.
 */
public class VersionTest
    extends TestCase
{
    public void testSnapshotWithoutRevision()
    {
        verify( packageVersion( "1.2-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            pkg( "1.2-20090423095107" ),
            deb( "1.2-20090423095107" ),
            rpm( "1.2_20090423095107", "1" ) );
    }

    public void testSnapshotWithConfiguredRevision()
    {
        verify( packageVersion( "1.2-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2-3-20090423095107" ),
            pkg( "1.2-3-20090423095107" ),
            rpm( "1.2_20090423095107", "3" ) );
    }

    public void testSnapshotWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            deb( "1.2-3-20090423095107" ),
            pkg( "1.2-3-20090423095107" ),
            rpm( "1.2_20090423095107", "3" ) );
    }

    public void testSnapshotWithEmbeddedAndConfiguredRevision()
    {
        verify( packageVersion( "1.2-3-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2-3-3-20090423095107" ),
            pkg( "1.2-3-3-20090423095107" ),
            rpm( "1.2_3_20090423095107", "3" ) );
    }

    // -----------------------------------------------------------------------
    // Release
    // -----------------------------------------------------------------------

    public void testReleaseWithoutRevision()
    {
        verify( packageVersion( "1.2", "20090423095107", false, Option.<String>none() ),
            deb( "1.2" ),
            pkg( "1.2" ),
            rpm( "1.2", "1" ) );
    }

    public void testReleaseWithConfiguredRevision()
    {
        verify( packageVersion( "1.2", "20090423095107", false, some( "3" ) ),
            deb( "1.2-3" ),
            pkg( "1.2-3" ),
            rpm( "1.2", "3" ) );
    }

    public void testReleaseWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3", "20090423095107", false, Option.<String>none() ),
            deb( "1.2-3" ),
            pkg( "1.2-3" ),
            rpm( "1.2", "3" ) );
    }

    public void testReleaseWithConfiguredAndEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3", "20090423095107", false, some( "3" ) ),
            deb( "1.2-3-3" ),
            pkg( "1.2-3-3" ),
            rpm( "1.2_3", "3" ) );
    }

    public static <A> void verify( A a, Verifier<A>... verifiers )
    {
        for ( Verifier<A> verifier : verifiers )
        {
            verifier.verify( a );
        }
    }

    private interface Verifier<A>
    {
        void verify( A a );
    }

    private Verifier<PackageVersion> deb( final String expected )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                assertEquals( "deb", expected, getDebianVersion( packageVersion ) );
            }
        };
    }

    private Verifier<PackageVersion> pkg( final String expected )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                assertEquals( "pkg", expected, getPkgVersion( packageVersion ) );
            }
        };
    }

    private Verifier<PackageVersion> rpm( final String expected, final String revision )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                P2<String, String> rpmVersion = RpmUnixPackage.getRpmVersion( packageVersion );
                assertEquals( "rpm.version", expected, rpmVersion._1() );
                assertEquals( "rpm.revision", revision, rpmVersion._2() );
            }
        };
    }
}
