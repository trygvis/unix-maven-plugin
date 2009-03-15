package org.codehaus.mojo.unix.pkg.prototype;

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

import fj.F;
import fj.data.Option;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.HasFileAttributes;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.Validate;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;

import java.io.File;
import static java.lang.Boolean.TRUE;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class PrototypeEntry
    implements LineProducer, HasFileAttributes<PrototypeEntry>
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
