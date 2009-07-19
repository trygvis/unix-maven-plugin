package org.codehaus.mojo.unix.maven.plugin;

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

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.FileSystem;
import org.apache.maven.artifact.*;
import org.apache.maven.plugin.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AssemblyOp
{
    private final Map<String, Artifact> mutableArtifactMap = new HashMap<String, Artifact>();

    protected final Map<String, Artifact> artifactMap = Collections.unmodifiableMap( mutableArtifactMap );

    protected final String operationType;

    protected AssemblyOp( String operationType )
    {
        this.operationType = operationType;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public abstract AssemblyOperation createOperation( FileObject basedir, FileAttributes defaultFileAttributes,
                                                       FileAttributes defaultDirectoryAttributes )
        throws MojoFailureException, FileSystemException, UnknownArtifactException;

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    public void setArtifactMap( Map<String, Artifact> artifactMap )
    {
        mutableArtifactMap.putAll( artifactMap );
    }

    protected FileObject resolve( FileSystem basedir, File file )
        throws FileSystemException
    {
        return basedir.resolveFile( file.getAbsolutePath() );
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
            throw new MojoFailureException( "Only one of '" + fieldA + "' and '" + fieldB + "' can be specified on a " + operationType + " operation." );
        }

        if ( valueA == null && valueB == null )
        {
            throw new MojoFailureException( "One of '" + fieldA + "' and '" + fieldB + "' has to be specified on a " + operationType + " operation." );
        }
    }

    protected RelativePath validateAndResolveOutputFile( File artifactFile, RelativePath toDir, RelativePath toFile )
        throws MojoFailureException
    {
        if ( toFile != null )
        {
            if ( toDir != null )
            {
                throw new MojoFailureException( "Can't specify both 'toDir' and 'toFile' on a " + operationType + " operation." );
            }

            return toFile;
        }

        if ( toDir == null )
        {
            toDir = RelativePath.BASE;
        }

        return toDir.add( artifactFile.getName() );
    }

    protected File validateFileIsReadableFile( File file, String fieldName )
        throws MojoFailureException
    {
        if ( !file.isFile() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + "' on an " + operationType +
                " operation is not a file: " + file.getAbsolutePath() );
        }

        if ( !file.canRead() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + " on an " +
                operationType + " operation is not readable " + file.getAbsolutePath() );
        }

        return file;
    }

    protected File validateFileIsDirectory( File file, String fieldName )
        throws MojoFailureException
    {
        if ( !file.isDirectory() )
        {
            throw new MojoFailureException( "The path specified in field '" + fieldName + "' on an " + operationType +
                " is not a directory: " + file.getAbsolutePath() );
        }

        return file;
    }

    protected File validateArtifact( String artifact )
        throws UnknownArtifactException
    {
        Artifact a = artifactMap.get( artifact );

        if ( a != null )
        {
            return a.getFile();
        }

        a = artifactMap.get( artifact + ":jar" );

        if ( a != null )
        {
            return a.getFile();
        }

        throw new UnknownArtifactException( artifact, artifactMap );
    }
}
