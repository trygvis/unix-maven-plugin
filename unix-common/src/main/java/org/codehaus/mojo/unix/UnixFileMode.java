package org.codehaus.mojo.unix;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnixFileMode
{
    /**
     * Mode equal to <code>-rw-r--r--</code>.
     */
    public static final UnixFileMode _0644 = UnixFileMode.fromInt( 0644 );

    /**
     * Mode equal to <code>-rwxr-xr-x</code>.
     */
    public static final UnixFileMode _0755 = UnixFileMode.fromInt( 0755 );

    private final int mode;

    public UnixFileMode( int mode )
    {
        this.mode = mode;
    }

    public int toInt()
    {
        return mode;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "         " );
        buffer.setCharAt( 0, ( mode & 0x100 ) > 0 ? 'r' : '-' );
        buffer.setCharAt( 1, ( mode & 0x080 ) > 0 ? 'w' : '-' );
        buffer.setCharAt( 2, ( mode & 0x040 ) > 0 ? 'x' : '-' );
        buffer.setCharAt( 3, ( mode & 0x020 ) > 0 ? 'r' : '-' );
        buffer.setCharAt( 4, ( mode & 0x010 ) > 0 ? 'w' : '-' );
        buffer.setCharAt( 5, ( mode & 0x400 ) > 0 ? 's' : ( mode & 0x008 ) > 0 ? 'x' : '-' );
        buffer.setCharAt( 6, ( mode & 0x004 ) > 0 ? 'r' : '-' );
        buffer.setCharAt( 7, ( mode & 0x002 ) > 0 ? 'w' : '-' );
        buffer.setCharAt( 8, ( mode & 0x001 ) > 0 ? 'x' : '-' );

        return buffer.toString();
    }

    public String toOctalString()
    {
        if ( mode < 10 )
        {
            return "000" + Integer.toOctalString( toInt() );
        }
        else if ( mode < 100 )
        {
            return "00" + Integer.toOctalString( toInt() );
        }
        else if ( mode < 1000 )
        {
            return "0" + Integer.toOctalString( toInt() );
        }

        return "0" + Integer.toOctalString( toInt() );
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static UnixFileMode fromInt( int mode )
    {
        return new UnixFileMode( mode );
    }

    public static UnixFileMode fromString( String string )
    {
        if ( string.length() != 9 )
        {
            throw new RuntimeException( "Illegal string format; string.length has to be 9 characters" );
        }

        int mode = 0;

        mode += expect( string, 0, 'r' );
        mode += expect( string, 1, 'w' );
        mode += expect( string, 2, 'x' );
        mode += expect( string, 3, 'r' );
        mode += expect( string, 4, 'w' );
        mode += expect( string, 5, 'x' );
        mode += expect( string, 6, 'r' );
        mode += expect( string, 7, 'w' );
        mode += expect( string, 8, 'x' );

        return new UnixFileMode( mode );
    }

    private static int expect( String string, int i, char one )
    {
        char c = string.charAt( i );

        if ( c == '-' )
        {
            return 0;
        }
        else if ( c == one )
        {
            return 1 << 8 - i;
        }

        throw new RuntimeException( "Illegal format, expected '-' or '" + c + "' at position " + i + "." );
    }
}
