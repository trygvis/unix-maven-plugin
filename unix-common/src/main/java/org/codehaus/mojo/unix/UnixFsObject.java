package org.codehaus.mojo.unix;

import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UnixFsObject
{
    public final RelativePath path;
    public final LocalDate lastModified;
    public final long size;
    public final FileAttributes attributes;

    public static final DateTimeFormatter formatter;

    static {
        formatter = new DateTimeFormatterBuilder().
            appendMonthOfYearShortText().
            appendLiteral( ' ' ).
            appendHourOfDay( 2 ).
            appendLiteral( ':' ).
            appendMinuteOfHour( 2 ).toFormatter();
    }

    protected UnixFsObject( RelativePath path, LocalDate lastModified, long size, FileAttributes attributes )
    {
        if ( attributes == null )
        {
            throw new NullPointerException( "attributes" );
        }
        
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.attributes = attributes;
    }

    public abstract boolean isDirectory();

    public abstract boolean isFile();

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

        return !( path != null ? !path.equals( that.path ) : that.path != null );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public static class FileUnixOFsbject
        extends UnixFsObject
    {
        public FileUnixOFsbject( RelativePath path, LocalDate lastModified, long size, FileAttributes attributes )
        {
            super( path, lastModified, size, attributes );
        }

        public boolean isDirectory()
        {
            return false;
        }

        public boolean isFile()
        {
            return true;
        }

        public String toString()
        {
            return "-" + (attributes.mode != null ? attributes.mode.toString() : "<unknown>") +
                " " + (attributes.user != null ? StringUtils.leftPad( attributes.user, 10 ) : " <unknown>") +
                " " + (attributes.group != null ? StringUtils.leftPad( attributes.group, 10 ) : " <unknown>") +
                " " + StringUtils.rightPad( String.valueOf( size ), 10 ) +
                " " + ( lastModified != null ? formatter.print( lastModified ) : "            " ) +
                path.string;
        }
    }

    public static class DirectoryUnixOFsbject
        extends UnixFsObject
    {
        public DirectoryUnixOFsbject( RelativePath path, LocalDate lastModified, FileAttributes attributes )
        {
            super( path, lastModified, 0, attributes );
        }

        public boolean isDirectory()
        {
            return true;
        }

        public boolean isFile()
        {
            return false;
        }

        public String toString()
        {
            return "-" + (attributes.mode != null ? attributes.mode.toString() : "<unknown>") +
                " " + (attributes.user != null ? StringUtils.leftPad( attributes.user, 10 ) : " <unknown>") +
                " " + (attributes.group != null ? StringUtils.leftPad( attributes.group, 10 ) : " <unknown>") +
                " " + "          " +
                " " + ( lastModified != null ? formatter.print( lastModified ) : "            " ) +
                path.string;
        }
    }
}
