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
import fj.data.Option;
import static fj.data.Option.fromNull;
import static org.codehaus.mojo.unix.UnixFileMode.showLong;
import static org.codehaus.mojo.unix.util.UnixUtil.optionEquals;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FileAttributes
{
    public final Option<String> user;

    public final Option<String> group;

    public final Option<UnixFileMode> mode;

    public final static Option<FileAttributes> none = Option.none();

    /**
     * A file object with all none fields. Use this when creating template objects.
     */
    public final static FileAttributes EMPTY = new FileAttributes( Option.<String>none(), Option.<String>none(),
        Option.<UnixFileMode>none() );

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

    public FileAttributes useAsDefaultsFor( FileAttributes other )
    {
        return new FileAttributes(
            other.user.orElse( user ),
            other.group.orElse( group ),
            other.mode.orElse( mode ) );
    }

    public final static F2<FileAttributes, FileAttributes, FileAttributes> useAsDefaultsFor = new F2<FileAttributes, FileAttributes, FileAttributes>()
    {
        public FileAttributes f( FileAttributes defaults, FileAttributes other )
        {
            return defaults.useAsDefaultsFor( other );
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
