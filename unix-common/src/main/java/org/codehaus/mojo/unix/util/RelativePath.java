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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import fj.pre.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RelativePath
    implements Comparable<RelativePath>
{
    public final String string;

    public final static RelativePath BASE = new RelativePath( "." )
    {
        public RelativePath add( String string )
        {
            String cleaned = clean( string );

            if ( cleaned == null )
            {
                return this;
            }

            return new RelativePath( cleaned );
        }

        public boolean isBelowOrSame( RelativePath other )
        {
            return other.isBase();
        }

        public Option<RelativePath> subtract( RelativePath parent )
        {
            if ( parent.isBase() )
            {
                return some( BASE );
            }

            return none();
        }

        public List<String> toList()
        {
            return nil();
        }

        public String asAbsolutePath( String basePath )
        {
            return basePath;
        }

        public boolean isBase()
        {
            return true;
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static final Ord<RelativePath> ord = Ord.ord( new F<RelativePath, F<RelativePath, Ordering>>()
    {
        public F<RelativePath, Ordering> f( final RelativePath a )
        {
            return new F<RelativePath, Ordering>()
            {
                public Ordering f( RelativePath b )
                {
                    return Ord.stringOrd.compare( a.string, b.string );
                }
            };
        }
    } );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private RelativePath( String string )
    {
        this.string = string;
    }

    public RelativePath add( String string )
    {
        string = clean( string );

        if ( string == null )
        {
            return this;
        }

        return new RelativePath( this.string + "/" + string );
    }

    public String asAbsolutePath( String basePath )
    {
        return basePath + ( basePath.endsWith( "/" ) ? "" : "/" ) + string;
    }

    public boolean isBase()
    {
        return false;
    }

    public String name()
    {
        int i = string.lastIndexOf( '/' );

        if ( i == -1 )
        {
            return string;
        }

        return string.substring( i + 1 );
    }

    /**
     * Returns true if <code>other</code> is further down the path than this path.
     * <p/>
     * <ul>
     * <li>".".isBelowOrSame(..) -> true. Everything is below the base path</li>
     * <li>"..".isBelowOrSame(".") -> true. Everything is below the base path</li>
     * <li>"/foo".isBelowOrSame( "/foo") -> true</li>
     * <li>"/foo/bar".isBelowOrSame( "/foo") -> true</li>
     * <li>"/foo".isBelowOrSame( "/foo/bar") -> false</li>
     * </ul>
     */
    public boolean isBelowOrSame( RelativePath parent )
    {
        return parent.isBase() || // Everything is below or equal to the base path
            string.startsWith( parent.string );
    }

    public Option<RelativePath> subtract( RelativePath parent )
    {
        if ( isBelowOrSame( parent ) )
        {
            if ( parent.isBase() || this.string.equals( parent.string ) )
            {
                return some( this );
            }

            return some( new RelativePath( this.string.substring( parent.string.length() + 1 ) ) );
        }

        return none();
    }

    public List<String> toList()
    {
        int i = string.lastIndexOf( '/' );

        if ( i == -1 )
        {
            return List.single( string );
        }

        List<String> list = List.single( string.substring( i + 1 ) );

        String s = string.substring( 0, i );

        do
        {
            i = s.lastIndexOf( '/' );

            if ( i == -1 )
            {
                return list.cons( s );
            }

            list = list.cons( s.substring( i + 1 ) );

            s = s.substring( 0, i );
        }
        while ( true );
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RelativePath relativePath( String string )
    {
        string = string == null ? "/" : string.trim();

        String s = clean( string );

        if ( s == null )
        {
            return BASE;
        }

        return new RelativePath( s );
    }

    static String clean( final String string )
    {
        String s = removeDuplicateSlashes( string );

        if ( isRoot( s ) )
        {
            return null;
        }

        if ( s.startsWith( "./" ) )
        {
            s = s.substring( 2 );
        }
        else if ( s.startsWith( "/" ) )
        {
            s = s.substring( 1 );
        }

        if ( s.endsWith( "/." ) )
        {
            s = s.substring( 0, s.length() - 2 );
        }
        else if ( s.endsWith( "/" ) )
        {
            s = s.substring( 0, s.length() - 1 );
        }

        if ( isRoot( s ) )
        {
            return null;
        }

        return s;
    }

    private static String removeDuplicateSlashes( String string )
    {
        StringBuffer buffer = new StringBuffer();

        boolean lastWasSlash = false;

        for ( int i = 0; i < string.length(); i++ )
        {
            char c = string.charAt( i );

            if ( c == '/' )
            {
                if ( !lastWasSlash )
                {
                    buffer.append( c );
                    lastWasSlash = true;
                }
            }
            else
            {
                buffer.append( c );
                lastWasSlash = false;
            }
        }

        return buffer.toString();
    }

    private static boolean isRoot( String s )
    {
        return s.length() == 0 || s.equals( "/" ) || s.equals( "." );
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
        if ( !( o instanceof RelativePath ) )
        {
            return false;
        }

        RelativePath path = (RelativePath) o;

        return string.equals( path.string );
    }

    public int hashCode()
    {
        return string.hashCode();
    }

    public String toString()
    {
        return string;
    }

    // -----------------------------------------------------------------------
    // Comparable
    // -----------------------------------------------------------------------

    public int compareTo( RelativePath other )
    {
        return string.compareTo( other.string );
    }
}
