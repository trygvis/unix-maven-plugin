package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.vfs.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AssemblyOperation
{
    private Map mutableArtifactMap = new HashMap();

    protected final Map artifactMap = Collections.unmodifiableMap( mutableArtifactMap );

    protected final String operationType;

    protected AssemblyOperation( String operationType )
    {
        this.operationType = operationType;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public abstract void perform( FileObject basedir, Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException;

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

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

        Map map = new TreeMap( artifactMap );

        System.out.println("Could not find artifact:" + artifact );
        System.out.println("Available artifacts:");
        for ( Iterator it = map.keySet().iterator(); it.hasNext(); )
        {
            System.out.println( it.next() );
        }

        throw new MojoFailureException( "Could not find artifact '" + artifact + "'." );
    }

    protected static void copyFiles( FileCollector fileCollector, FileObject fromDir, RelativePath toDir,
                                     List includes, List excludes,
                                     String patternString, String replacement,
                                     FileAttributes fileAttributes, FileAttributes directoryAttributes )
        throws IOException
    {
        Pattern pattern = patternString != null ? Pattern.compile( patternString ) : null;

        IncludeExcludeFileSelector selector = IncludeExcludeFileSelector.build( fromDir.getName() ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        List files = new ArrayList();
        fromDir.findFiles( selector, true, files );

        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            FileObject f = (FileObject) it.next();

            if ( f.getName().getBaseName().equals( "" ))
            {
                continue;
            }

            String relativeName = fromDir.getName().getRelativeName( f.getName() );

            // Transform the path if the pattern is set. The input path will always have a leading slash
            // to make it possible to write more natural expressions.
            // With this one can write "/server-1.0.0/(.*)" => $1
            if ( pattern != null )
            {
                relativeName = pattern.matcher( prefixWithSlash( relativeName )).replaceAll( replacement );
            }

            if ( f.getType() == FileType.FILE )
            {
                fileCollector.addFile( f, toDir.add( relativeName ), fileAttributes );
            }
            else if ( f.getType() == FileType.FOLDER )
            {
                fileCollector.addDirectory( toDir.add( relativeName ), directoryAttributes );
            }
        }
    }

    public static String prefixWithSlash( String s )
    {
        return s.startsWith( "/" ) ? s : "/" + s;
    }
}
