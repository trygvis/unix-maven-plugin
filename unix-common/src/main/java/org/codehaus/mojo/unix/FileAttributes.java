package org.codehaus.mojo.unix;

import org.codehaus.plexus.util.StringUtils;

public class FileAttributes
{
    public final String user;

    public final String group;

    public final UnixFileMode mode;

    public final static FileAttributes UNKNOWN = new FileAttributes();

    public FileAttributes()
    {
        this( null, null, null );
    }

    public FileAttributes( String user, String group, UnixFileMode mode )
    {
        this.user = user;
        this.group = group;
        this.mode = mode;
    }

    public FileAttributes useAsDefaultsFor( FileAttributes other )
    {
        if ( other == null )
        {
            other = new FileAttributes();
        }

        return new FileAttributes(
            StringUtils.defaultString( other.user, user ),
            StringUtils.defaultString( other.group, group ),
            other.mode != null ? other.mode : mode );
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        FileAttributes that = (FileAttributes) o;

        return ( user != null ? user.equals( that.user ) : that.user == null ) &&
            ( group != null ? group.equals( that.group ) : that.group == null ) &&
            ( mode != null ? mode.equals( that.mode ) : that.mode == null );
    }

    public String toString()
    {
        return "user=" + StringUtils.defaultString( user, "<not set>" ) + ", " +
            "group=" + StringUtils.defaultString( group, "<not set>" ) + ", " +
            "mode=" + StringUtils.defaultString( mode, "<not set>" );
    }
}
