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

import fj.F;
import fj.F2;
import static fj.Function.curry;
import static fj.P.p;
import fj.data.Option;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.UnixUtil;
import org.codehaus.mojo.unix.util.Validate;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UnixFsObject<A extends UnixFsObject>
    implements LineProducer
{
    public final RelativePath path;
    public final LocalDateTime lastModified;
    public final long size;
    public final Option<FileAttributes> attributes;

    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().
        appendMonthOfYearShortText().
        appendLiteral( ' ' ).
        appendHourOfDay( 2 ).
        appendLiteral( ':' ).
        appendMinuteOfHour( 2 ).toFormatter();

    public static final F<LocalDateTime, String> formatter = curry( UnixUtil.formatLocalDateTime, FORMAT );

    private char prefixChar;

    protected UnixFsObject( char prefixChar, RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes )
    {
        validateNotNull( path, lastModified, attributes );

        this.prefixChar = prefixChar;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.attributes = attributes;
    }

    public final A setPath( RelativePath path )
    {
        return copy( path, lastModified, size, attributes );
    }

    public final A setLastModified( LocalDateTime lastModified )
    {
        return copy( path, lastModified, size, attributes );
    }

    protected abstract A copy( RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes );

    public UnixFsObject cast()
    {
        return this;
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RegularFile regularFile( RelativePath path, LocalDateTime lastModified, long size,
                                           Option<FileAttributes> attributes )
    {
        return new RegularFile( path, lastModified, size, attributes );
    }

    public static Directory directory( RelativePath path, LocalDateTime lastModified )
    {
        return new Directory( path, lastModified, Option.<FileAttributes>none() );
    }

    public static Directory directory( RelativePath path, LocalDateTime lastModified, FileAttributes attributes )
    {
        return new Directory( path, lastModified, some( attributes ) );
    }

    public static Symlink symlink( RelativePath from, LocalDateTime lastModified, Option<FileAttributes> attributes,
                                   String to )
    {
        return new Symlink( from, lastModified, attributes, to );
    }

    public A setAttributes( FileAttributes attributes )
    {
        Validate.validateNotNull( attributes );
        return copy( path, lastModified, size, some( attributes ) );
    }

    public static F2<UnixFsObject, FileAttributes, UnixFsObject> setAttributes()
    {
        return new F2<UnixFsObject, FileAttributes, UnixFsObject>()
        {
            public UnixFsObject f( UnixFsObject unixFsObject, FileAttributes fileAttributes )
            {
                return unixFsObject.setAttributes( fileAttributes );
            }
        };
    }

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof UnixFsObject ) )
        {
            return false;
        }

        UnixFsObject that = (UnixFsObject) o;

        return path.equals( that.path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public void streamTo( LineStreamWriter stream )
    {
        stream.add( toString() );
    }

    public String toString()
    {
        F<String,String> leftPad10 = curry( UnixFsObject.leftPad, 10 );
        F<String,String> rightPad10 = curry( UnixFsObject.rightPad, 10 );

        System.out.println( "this.path = " + this.path );
        // I wonder how long this will hold. Perhaps it should only be possible to call toString() on valid
        // objects - trygve
        FileAttributes attributes = this.attributes.some();

        return prefixChar + attributes.mode.map( UnixFileMode.showLong ).orSome( "<unknown>" ) +
            " " + attributes.user.map( leftPad10 ).orSome( " <unknown>" ) +
            " " + attributes.group.map( leftPad10 ).orSome( " <unknown>" ) +
            " " + p( String.valueOf( size ) ).map( rightPad10 )._1() +
            " " + p( lastModified ).map( formatter )._1() +
            " " + path.string;
    }

    // -----------------------------------------------------------------------
    // Sub classes
    // -----------------------------------------------------------------------

    public static class RegularFile
        extends UnixFsObject<RegularFile>
    {
        private RegularFile( RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes )
        {
            super( '-', path, lastModified, size, attributes );
        }

        protected RegularFile copy( RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes )
        {
            return new RegularFile( path, lastModified, size, attributes );
        }
    }

    public static class Directory
        extends UnixFsObject<Directory>
    {
        private Directory( RelativePath path, LocalDateTime lastModified, Option<FileAttributes> attributes )
        {
            super( 'd', path, lastModified, 0, attributes );
        }

        protected Directory copy( RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes )
        {
            return new Directory( path, lastModified, attributes );
        }
    }

    public static class Symlink
        extends UnixFsObject<Symlink>
    {
        public final String target;

        private Symlink( RelativePath from, LocalDateTime lastModified, Option<FileAttributes> attributes, String target )
        {
            super( 'l', from, lastModified, sizeOfSymlink( target ), attributes );

            this.target = target;
        }

        private static long sizeOfSymlink( String to )
        {
            // This might not be good enough validation
            int i = to.lastIndexOf( '/' );

            return (i == -1 ) ? to.length() : to.length() - i - 1;
        }

        protected Symlink copy( RelativePath path, LocalDateTime lastModified, long size, Option<FileAttributes> attributes )
        {
            return new Symlink( path, lastModified, attributes, target );
        }
    }

    private static final F2<Integer, String, String> leftPad = new F2<Integer, String, String>()
    {
        public String f( Integer size, String s )
        {
            return StringUtils.leftPad( s, size );
        }
    };

    private static final F2<Integer, String, String> rightPad = new F2<Integer, String, String>()
    {
        public String f( Integer size, String s )
        {
            return StringUtils.leftPad( s, size );
        }
    };
}
