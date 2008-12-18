package org.codehaus.mojo.unix;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public final class PackageVersion
{
    public final String version;

    public final String timestamp;

    public final boolean snapshot;

    public final int revision;

    private PackageVersion( String version, String timestamp, boolean snapshot, int revision )
    {
        this.version = version;
        this.timestamp = timestamp;
        this.snapshot = snapshot;
        this.revision = revision;
    }

    /**
     * This function parses the fields that is given in a Maven project
     */
    static PackageVersion fromMavenProject( String version, String timestamp, boolean snapshot )
    {
        if ( snapshot )
        {
            if ( timestamp == null )
            {
                throw new RuntimeException( "The timestamp can't be null when creating a snapshot version" );
            }

        }

        version = stripSnapshot( snapshot, version );

        int maintainerRevision = -1;

        int index = version.lastIndexOf( '-' );

        if ( index != -1 )
        {
            try
            {
                maintainerRevision = Integer.parseInt( version.substring( index + 1 ) );

                version = version.substring( 0, index );
            }
            catch ( NumberFormatException e )
            {
                maintainerRevision = -1;
            }
        }

        return new PackageVersion( version, timestamp, snapshot, maintainerRevision );
    }

    private static String stripSnapshot( boolean snapshot, String version )
    {
        if( !snapshot )
        {
            return version;
        }

        // Try to extract the maintainerRevision from the pom
        if ( !version.endsWith( "-SNAPSHOT" ) )
        {
            throw new RuntimeException(
                "Expected the version string to end with '-SNAPSHOT' when the version is a snapshot version." );
        }

        return version.substring( 0, version.length() - 9 );
    }

    public static PackageVersion create( String version, String timestamp, boolean snapshot,
                                         String configuredVersion, Integer configuredRevision )
    {
        if ( version == null )
        {
            throw new RuntimeException( "version == null" );
        }

        if ( timestamp == null )
        {
            throw new RuntimeException( "timestamp == null" );
        }

        // If the configured revision is set, there is not need to transform the version
        if ( configuredRevision != null )
        {
            return new PackageVersion( stripSnapshot( snapshot, version ), timestamp, snapshot,
                configuredRevision.intValue() );
        }

        PackageVersion v = fromMavenProject( version, timestamp, snapshot );

        version = StringUtils.isNotEmpty( configuredVersion ) ? configuredVersion : v.version;

        switch ( v.revision )
        {
            case -1:
            case 0:
                return new PackageVersion( version, v.timestamp, v.snapshot, 1 );
            default:
                return new PackageVersion( version, v.timestamp, v.snapshot, v.revision );
        }
    }

    public String getMavenVersion()
    {
        String v = version + "-" + revision;

        if ( snapshot )
        {
            return v + "-SNAPSHOT";
        }

        return v;
    }
}
