package org.codehaus.mojo.unix;

import fj.F;
import fj.F2;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static org.codehaus.mojo.unix.UnixFileMode.showLong;
import static org.codehaus.mojo.unix.util.UnixUtil.optionEquals;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: IEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class FileAttributes
{
    public final Option<String> user;

    public final Option<String> group;

    public final Option<UnixFileMode> mode;

    public final static Option<FileAttributes> none = Option.none();

    public FileAttributes()
    {
        this( Option.<String>none(), Option.<String>none(), Option.<UnixFileMode>none() );
    }

    public FileAttributes( Option<String> user, Option<String> group, Option<UnixFileMode> mode )
    {
        validateNotNull( user, group, mode );
        this.user = user;
        this.group = group;
        this.mode = mode;
    }

    public FileAttributes user( String user )
    {
        return new FileAttributes( fromNull( user ), group, mode );
    }

    public FileAttributes user( Option<String> user )
    {
        return new FileAttributes( user, group, mode );
    }

    public FileAttributes group( String group )
    {
        return new FileAttributes( user, fromNull( group ), mode );
    }

    public FileAttributes group( Option<String> group )
    {
        return new FileAttributes( user, group, mode );
    }

    public FileAttributes mode( UnixFileMode mode )
    {
        return new FileAttributes( user, group, fromNull( mode ) );
    }

    public FileAttributes mode( Option<UnixFileMode> mode )
    {
        return new FileAttributes( user, group, mode );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public final static F2<FileAttributes, FileAttributes, FileAttributes> useAsDefaultsFor = new F2<FileAttributes, FileAttributes, FileAttributes>()
    {
        public FileAttributes f( FileAttributes defaults, FileAttributes other )
        {
            return new FileAttributes(
                other.user.orElse( defaults.user ),
                other.group.orElse( defaults.group ),
                other.mode.orElse( defaults.mode ) );
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public final static F<FileAttributes, Option<String>> userF = new F<FileAttributes, Option<String>>()
    {
        public Option<String> f( FileAttributes attributes )
        {
            return attributes.user;
        }
    };

    public final static F<FileAttributes, Option<String>> groupF = new F<FileAttributes, Option<String>>()
    {
        public Option<String> f( FileAttributes attributes )
        {
            return attributes.group;
        }
    };

    public final static F<FileAttributes, Option<UnixFileMode>> modeF = new F<FileAttributes, Option<UnixFileMode>>()
    {
        public Option<UnixFileMode> f( FileAttributes attributes )
        {
            return attributes.mode;
        }
    };

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

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

        return optionEquals( user, that.user ) &&
            optionEquals( group, that.group ) &&
            optionEquals( mode, that.mode );
    }

    public String toString()
    {
        return "user=" + user.orSome( "<not set>" )+ ", " +
            "group=" + group.orSome( "<not set>" )+ ", " +
            "mode=" + mode.map( showLong ).orSome( "<not set>" );
    }
}
