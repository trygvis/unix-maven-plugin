package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AssemblyOperation
{
    private Map mutableArtifactMap = new HashMap();

    protected final Map artifactMap = Collections.unmodifiableMap( mutableArtifactMap );

    private static FileSystemManager fsManager;

    protected final String operationType;

    protected AssemblyOperation( String operationType )
    {
        this.operationType = operationType;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public abstract void perform( Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException;

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    public static FileSystemManager getFsManager()
        throws IOException
    {
        if ( fsManager == null )
        {
            fsManager = VFS.getManager();
        }

        return fsManager;
    }

    public void setArtifactMap( Map artifactMap )
    {
        mutableArtifactMap.putAll( artifactMap );
    }

    protected static String nullifEmpty( String artifact )
    {
        return StringUtils.clean( artifact ).length() == 0 ? null : artifact;
    }

    protected void validateIsSet( Object valueA, String fieldA )
        throws MojoFailureException
    {
        if ( valueA == null )
        {
            throw new MojoFailureException( "Field '" + fieldA + "' has to be specified on a " + operationType + " operation." );
        }
    }

    protected void validateEitherIsSet( Object valueA, Object valueB, String fieldA, String fieldB )
        throws MojoFailureException
    {
        if ( valueA != null && valueB != null )
        {
            throw new MojoFailureException( "Only one of '" + fieldA + "' or '" + fieldB + "' can be specified on a " + operationType + " operation." );
        }

        if ( valueA == null && valueB == null )
        {
            throw new MojoFailureException( "Either '" + fieldA + "' or '" + fieldB + "' has to be specified on a " + operationType + " operation." );
        }
    }

    protected Artifact artifact( String artifact )
        throws MojoFailureException
    {
        Artifact a = (Artifact) artifactMap.get( artifact );

        if ( a != null )
        {
            return a;
        }

        a = (Artifact) artifactMap.get( artifact + ":jar" );

        if ( a != null )
        {
            return a;
        }

        throw new MojoFailureException( "Could not find artifact '" + artifact + "'." );
    }
}
