package org.codehaus.mojo.unix.util;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RelativePath
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

        public String asAbsolutePath()
        {
            return "/";
        }
    };

    private RelativePath( String string )
    {
        this.string = string;
    }

    public RelativePath add( String string )
    {
        string = clean( string );

        if( string == null )
        {
            return this;
        }

        return new RelativePath( this.string + "/" + string );
    }

    public static RelativePath fromString( String string )
    {
        string = string == null ? "/" : string.trim();

        String s = clean( string );

        if ( s == null )
        {
            return BASE;
        }

        return new RelativePath( s );
    }

    public String asAbsolutePath()
    {
        return "/" + string;
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

//        System.out.println( StringUtils.rightPad( string, 30 )+ " => " + StringUtils.rightPad( s, 30 )+ " => " + s );

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
}
