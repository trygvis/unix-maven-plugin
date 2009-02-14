package org.codehaus.mojo.unix;

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
        PackageVersion.create( "1.0-alpha-2", "20080703.084400", false, null, new Integer( 3 ) ) );
    }

    public void testSnapshotVersionWithoutEmbeddedRevison()
    {
        // When your project uses versions on the form "1.0-alpha-2" you have to specify the revision
        assertVersion( "1.0-alpha-2", "20080703.084400", 3, "1.0-alpha-2-3-SNAPSHOT",
            PackageVersion.create( "1.0-alpha-2-SNAPSHOT", "20080703.084400", true, null, new Integer( 3 ) ) );
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
