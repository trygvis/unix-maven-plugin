package org.codehaus.mojo.unix.ar;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArUtil
{
    public static final String LF = "\n";
    public static final String AR_ARCHIVE_MAGIC = "!<arch>" + LF;
    public static final String AR_FILE_MAGIC = "`\n";
    public static final String US_ASCII = "US-ASCII";

    public static String convertString( byte[] bytes, int start, int count )
        throws UnsupportedEncodingException
    {
        String s = new String( bytes, start, count, ArUtil.US_ASCII );

        int index = s.indexOf( ' ' );

        if ( index == -1 )
        {
            return s;
        }

        s = s.substring( 0, index );

        return s;
    }

    public static byte[] readBytes( InputStream is, long count )
        throws IOException
    {
        byte[] bytes = new byte[(int) count];

        int start = 0;

        do
        {
            int read = is.read( bytes, start, (int) count );

            // If we're at EOF, but trying to read the first set of bytes, return null
            if ( read == -1 )
            {
                if ( start > 0 )
                {
                    throw new EOFException();
                }

                return null;
            }

            start += read;
            count -= read;
        }
        while ( count > 0 );

        return bytes;
    }

    public static void skipBytes( InputStream is, long count )
        throws IOException
    {
        long left = count;

        do
        {
            long read = is.skip( left );

            left -= read;
        }
        while ( left > 0 );
    }

    public static void copy( InputStream input, OutputStream output, int bufferSize )
        throws IOException
    {
        final byte[] buffer = new byte[bufferSize];
        int n;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }

    public static void close( ArWriter writer )
    {
        try
        {
            writer.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    public static void close( CloseableIterable reader )
    {
        if ( reader != null )
        {
            reader.close();
        }
    }
}
