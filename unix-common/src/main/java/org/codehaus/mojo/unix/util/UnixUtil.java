package org.codehaus.mojo.unix.util;

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

import fj.Bottom;
import fj.F;
import fj.F2;
import static fj.Function.compose;
import static fj.Function.curry;
import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.java.ClassF;
import org.codehaus.mojo.unix.java.ObjectF;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnixUtil
{
    public static final Option<String> noneString = Option.none();

    public static final Option<Boolean> noneBoolean = Option.none();

    public static void assertField( String field, Object value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }
    }

    public static String getField( String field, String value )
        throws MissingSettingException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static File getField( String field, File value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static String getFieldOrDefault( String value, String defaultValue )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            return defaultValue;
        }

        return value;
    }

    // -----------------------------------------------------------------------
    // Unix commands
    // -----------------------------------------------------------------------

    public static void chmodIf( boolean b, File file, String mode )
        throws IOException
    {
        if ( b )
        {
            chmod( file, mode );
        }
    }

    public static void chmod( File file, String mode )
        throws IOException
    {
        new SystemCommand().
            setCommand( "chmod" ).
            addArgument( mode ).
            addArgument( file.getAbsolutePath() ).
            execute().
            assertSuccess( "Error while running chmod on " + file );
    }

    public static void symlink( File basedir, String source, RelativePath target )
        throws IOException
    {
        new SystemCommand().
//            dumpCommandIf( true ).
            setBasedir( basedir ).
            setCommand( "ln" ).
            addArgument( "-s" ).
            addArgument( source ).
            addArgument( target.string ).
            execute().
            assertSuccess( "Error while running ln -s in " + basedir.getAbsolutePath() );
    }

    // -----------------------------------------------------------------------
    // Functional Java
    // -----------------------------------------------------------------------

    public static <A, B> Iterator<B> iteratorMap( final F<A, B> f, final Iterator<A> iterator ) {
        return new Iterator<B>()
        {
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            public B next()
            {
                return f.f(iterator.next());
            }

            public void remove()
            {
                iterator.remove();
            }
        };
    }

    public static <A> boolean optionEquals( Option<A> tis, java.lang.Object o )
    {
        if ( o == null || !( o instanceof Option ) )
        {
            return false;
        }

        return o == tis || optionEquals( tis, (Option) o );
    }

    public static <A> boolean optionEquals( Option<A> tis, Option that )
    {
        // This logic would be in None
        if ( tis.isNone() )
        {
            return that.isNone();
        }

        if ( that.isNone() )
        {
            return false;
        }

        // This logic would be in Some
        return tis.some().equals( that.some() );
    }

    public static <A> F<Option<A>, Boolean> isSome_() {
        return new F<Option<A>, Boolean>()
        {
            public Boolean f( Option<A> option )
            {
                return option.isSome();
            }
        };
    }

    public static <A> A someE( Option<A> option, String msg ) {
        if( option.isSome() ) {
            return option.some();
        }

        throw Bottom.error( msg );
    }

    public static final F2<DateTimeFormatter, LocalDateTime, String> formatLocalDateTime =
        new F2<DateTimeFormatter, LocalDateTime, String>()
        {
            public String f( DateTimeFormatter dateTimeFormatter, LocalDateTime partial )
            {
                return dateTimeFormatter.print( partial );
            }
        };

    public static final F2<DateTimeFormatter, String, Option<DateTime>> parseDateTime =
        new F2<DateTimeFormatter, String, Option<DateTime>>()
        {
            public Option<DateTime> f( DateTimeFormatter dateTimeFormatter, String text )
            {
                try
                {
                    return some( dateTimeFormatter.parseDateTime( text ) );
                }
                catch ( IllegalArgumentException e )
                {
                    return none();
                }
            }
        };

    public static final F<DateTime, LocalDateTime> toLocalDateTime = new F<DateTime, LocalDateTime>()
    {
        public LocalDateTime f( DateTime dateTime )
        {
            return dateTime.toLocalDateTime();
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static void close( Closeable closeable )
    {
        try
        {
            closeable.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    public static void flush( Flushable flusable )
    {
        try
        {
            flusable.flush();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    // ----------------------------------------------------------------------
    // Helper methods for test methods
    // ----------------------------------------------------------------------

    public static File getTestFile( String path )
    {
        return new File( basedir, path );
    }

    public static String getTestPath( String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    private final static File basedir = getBasedir();

    public static File getBasedir()
    {
        return new File( System.getProperty( "basedir", new File( "" ).getAbsolutePath() ) );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static final class Filter
    {
        public static <T> F<T, Boolean> instanceOfFilter(java.lang.Class cls)
        {
            return compose( curry( ClassF.isAssignableFrom, cls ), ObjectF.<T>getClass_() );
        }
    }
}
