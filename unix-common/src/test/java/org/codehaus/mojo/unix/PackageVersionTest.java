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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageVersionTest
    extends TestCase
{
    public void testReleaseVersionWithEmbeddedRevision()
    {
        assertVersion( "1.0", "20080703.084400", 1, "1.0-1",
            PackageVersion.create( "1.0-1", "20080703.084400", false, null, null ) );
    }

    public void testReleaseVersionWithoutEmbeddedRevison()
    {
        assertVersion( "1.0", "20080703.084400", 1, "1.0-1",
        PackageVersion.create( "1.0", "20080703.084400", false, null, null ) );
    }

    public void testReleaseVersionWithConfiguredRevison()
    {
        assertVersion( "1.0-alpha-2", "20080703.084400", 3, "1.0-alpha-2-3",
        PackageVersion.create( "1.0-alpha-2", "20080703.084400", false, null, 3 ) );
    }

    public void testSnapshotVersionWithoutEmbeddedRevison()
    {
        // When your project uses versions on the form "1.0-alpha-2" you have to specify the revision
        assertVersion( "1.0-alpha-2", "20080703.084400", 3, "1.0-alpha-2-3-SNAPSHOT",
            PackageVersion.create( "1.0-alpha-2-SNAPSHOT", "20080703.084400", true, null, 3 ) );
    }

    public void testRevision()
        throws Exception
    {
        assertVersion( "1.0", "20080703.084400", 2, "1.0-2",
            PackageVersion.create( "1.0-2", "20080703.084400", false, null, null ) );

        assertVersion( "1.0-alpha", "20080703.084400", 2, "1.0-alpha-2-SNAPSHOT",
            PackageVersion.create( "1.0-alpha-2-SNAPSHOT", "20080703.084400", true, null, null ) );

        // Hm, should this be allowed? Creating non-snapshot artifacts from snapshot artifacts.
        assertVersion( "1.0", "20080703.084400", 2, "1.0-2-SNAPSHOT",
            PackageVersion.create( "1.0-2-SNAPSHOT", "20080703.084400", true, null, null ) );
    }

    public void testThatTimestampIsRequired()
    {
        try
        {
            PackageVersion.create( "1.0-SNAPSHOT", null, true, null, null );
            fail( "Expected error" );
        }
        catch ( RuntimeException e )
        {
            assertTrue( e.getMessage().indexOf( "timestamp == null" ) != -1 );
        }
    }

    private void assertVersion( String version, String timestamp, int revision, String mavenVersion,
                                PackageVersion packageVersion )
    {
        assertEquals( "version", version, packageVersion.version );
        assertEquals( "timestamp", timestamp, packageVersion.timestamp );
        assertEquals( "revision", revision, packageVersion.revision );
        assertEquals( "mavenVersion", mavenVersion, packageVersion.getMavenVersion() );
    }
}
