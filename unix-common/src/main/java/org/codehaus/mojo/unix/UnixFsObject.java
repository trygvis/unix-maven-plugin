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

import fj.*;
import static fj.Function.*;
import static fj.P.*;
import fj.data.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.Validate.*;
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import org.joda.time.format.*;

import java.util.regex.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class UnixFsObject<A extends UnixFsObject>
    implements Comparable<UnixFsObject>, LineProducer
{
    public final RelativePath path;
    public final LocalDateTime lastModified;
    public final long size;
    public final FileAttributes attributes;
    public final List<Filter> filters;

    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().
        appendMonthOfYearShortText().
        appendLiteral( ' ' ).
        appendHourOfDay( 2 ).
        appendLiteral( ':' ).
        appendMinuteOfHour( 2 ).toFormatter();

    public static final F<LocalDateTime, String> formatter = curry( UnixUtil.formatLocalDateTime, FORMAT );

    private char prefixChar;

    protected UnixFsObject( char prefixChar, RelativePath path, LocalDateTime lastModified, long size,
                            FileAttributes attributes, List<Filter> filters )
    {
        validateNotNull( path, lastModified, attributes, filters );

        this.prefixChar = prefixChar;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.attributes = attributes;
        this.filters = filters;
    }

    public final A setPath( RelativePath path )
    {
        return copy( path, lastModified, size, attributes, filters );
    }

    public final A setLastModified( LocalDateTime lastModified )
    {
        return copy( path, lastModified, size, attributes, filters );
    }

    public FileAttributes getFileAttributes()
    {
        return attributes;
    }

    public final A setFileAttributes( FileAttributes attributes )
    {
        Validate.validateNotNull( attributes );
        return copy( path, lastModified, size, attributes, filters );
    }

    public final A withFilters( List<Filter> filters )
    {
        return copy( path, lastModified, size, attributes, filters );
    }

    protected abstract A copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes, List<Filter> filters );

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RegularFile regularFile( RelativePath path, LocalDateTime lastModified, long size,
                                           FileAttributes attributes )
    {
        return new RegularFile( path, lastModified, size, attributes, List.<Filter>nil() );
    }

    public static RegularFile regularFile( RelativePath path, LocalDateTime lastModified, long size,
                                           FileAttributes attributes, List<Filter> filters )
    {
        return new RegularFile( path, lastModified, size, attributes, filters );
    }

    public static Directory directory( RelativePath path, LocalDateTime lastModified, FileAttributes attributes )
    {
        return new Directory( path, lastModified, attributes, List.<Filter>nil() );
    }

    public static Symlink symlink( RelativePath path, LocalDateTime lastModified, FileAttributes attributes,
                                   String target )
    {
        return new Symlink( path, lastModified, attributes, target, List.<Filter>nil() );
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
        if ( !(o instanceof UnixFsObject) )
        {
            return false;
        }

        UnixFsObject that = (UnixFsObject) o;

        return path.equals( that.path ) &&
            lastModified.equals( that.lastModified ) &&
            size == that.size &&
            attributes.equals( that.attributes );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public void streamTo( LineStreamWriter stream )
    {
        stream.add( toString() );
    }

    public int compareTo( UnixFsObject other )
    {
        return path.compareTo( other.path );
    }

    public String toString()
    {
        F<String, String> leftPad10 = curry( UnixFsObject.leftPad, 10 );
        F<String, String> rightPad10 = curry( UnixFsObject.rightPad, 10 );

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
        private RegularFile( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes, List<Filter> filters )
        {
            super( '-', path, lastModified, size, attributes, filters );
        }

        protected RegularFile copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes, List<Filter> filters )
        {
            return new RegularFile( path, lastModified, size, attributes, filters );
        }
    }

    public static class Directory
        extends UnixFsObject<Directory>
    {
        private Directory( RelativePath path, LocalDateTime lastModified, FileAttributes attributes, List<Filter> filters )
        {
            super( 'd', path, lastModified, 0, attributes, filters );
        }

        protected Directory copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes, List<Filter> filters )
        {
            return new Directory( path, lastModified, attributes, filters );
        }
    }

    public static class Symlink
        extends UnixFsObject<Symlink>
    {
        public final String value;

        private Symlink( RelativePath path, LocalDateTime lastModified, FileAttributes attributes, String value, List<Filter> filters )
        {
            super( 'l', path, lastModified, sizeOfSymlink( value ), attributes, filters );
            validateNotNull( value );

            this.value = value;
        }

        private static long sizeOfSymlink( String s )
        {
            // This might not be good enough validation
            int i = s.lastIndexOf( '/' );

            return (i == -1) ? s.length() : s.length() - i - 1;
        }

        protected Symlink copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes, List<Filter> filters )
        {
            return new Symlink( path, lastModified, attributes, value, filters );
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

    public static class Filter
    {
        public final Pattern pattern;

        public final String replacement;

        public static Show<Filter> filterShow = Show.anyShow();

        public Filter( Pattern pattern, String replacement )
        {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String toString()
        {
            return "s/" + pattern.pattern() + "/" + replacement + "/";
        }

        public String replace( String line )
        {
            return pattern.matcher( line ).replaceAll( replacement );
        }
    }
}
