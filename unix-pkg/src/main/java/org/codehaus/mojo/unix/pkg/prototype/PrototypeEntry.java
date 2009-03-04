package org.codehaus.mojo.unix.pkg.prototype;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import fj.data.Option;
import static fj.data.Option.some;
import fj.F;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.Validate;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;

import java.io.File;
import static java.lang.Boolean.TRUE;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: AbstractPrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public abstract class PrototypeEntry
    implements LineProducer
{
    static final String EOL = System.getProperty( "line.separator" );

    protected final String pkgClass;

    protected final Option<Boolean> relative;

    protected final RelativePath path;

    public static PrototypeEntry fromLine( String line )
    {
        String[] parts = line.split( " " );

        if ( parts.length < 6 )
        {
            throw new RuntimeException( "Invalid line, expected at least 6 parts." );
        }

        String type = parts[0];

        if ( "f".equals( type ) )
        {
            FileAttributes attributes = new FileAttributes( some( parts[3] ), some( parts[4] ), some( UnixFileMode.fromString( parts[2] ) ) );

            if ( parts.length != 7 )
            {
                throw new RuntimeException( "parts.length != 7" );
            }

            return new FileEntry( some( parts[1] ), fromString( parts[6] ), attributes );
//            if ( parts.length == 7 )
//            {
//                return new FileEntry( parts[1], fromString( parts[6] ), attributes );
//            }
//            else
//            {
//                return new FileEntry( parts[1], none(), attributes );
//            }
        }
        else
        {
            throw new RuntimeException( "Unknown file type '" + type + "'." );
        }
    }

    protected PrototypeEntry( Option<String> pkgClass, Option<Boolean> relative, RelativePath path )
    {
        Validate.validateNotNull( pkgClass, relative, path );
        this.pkgClass = pkgClass.orSome( "none" );
        this.relative = relative;
        this.path = path;
    }

    protected abstract String generatePrototypeLine();

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void streamTo( LineStreamWriter stream )
    {
        stream.
            add( generatePrototypeLine() );
    }

    public String getPath()
    {
        if ( TRUE.equals( relative.orSome( false ) ) )
        {
            return path.string;
        }

        return path.asAbsolutePath( "/" );
    }

    // -----------------------------------------------------------------------
    // ObjectF Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || PrototypeEntry.class != o.getClass() )
        {
            return false;
        }

        PrototypeEntry that = (PrototypeEntry) o;

        return path.equals( that.path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected String getProcessedPath( Option<File> realPath )
    {
        return realPath.map( new F<File, String>()
        {
            public String f( File file )
            {
                return getPath() + "=" + file.getAbsolutePath();
            }
        } ).orSome( getPath() );
    }

    protected String toString( FileAttributes attributes )
    {
        return
            FileEntry.getModeString( attributes ) +
            " " + attributes.user.orSome( "?" ) +
            " " + attributes.group.orSome( "?" );
    }
}
