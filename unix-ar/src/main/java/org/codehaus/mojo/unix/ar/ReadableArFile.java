package org.codehaus.mojo.unix.ar;

import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ReadableArFile
    extends ArFile
{
    private InputStream inputStream;
    private ArFileInputStream fileInputStream;
    private long left;

    public ReadableArFile( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public InputStream open()
    {
        if ( inputStream == null )
        {
            throw new RuntimeException( "This file has already been read" );
        }

        return fileInputStream = new ArFileInputStream();
    }

    void close()
    {
        // If the file havent been opened, skip the bytes
        if ( fileInputStream == null )
        {
            fileInputStream = new ArFileInputStream();
        }

        IOUtil.close( fileInputStream );
    }

    private class ArFileInputStream
        extends InputStream
    {
        private InputStream inputStream;

        public ArFileInputStream()
        {
            this.inputStream = ReadableArFile.this.inputStream;
            ReadableArFile.this.inputStream = null;
            left = size;
        }

        public int read()
            throws IOException
        {
            if ( left <= 0 )
            {
                return -1;
            }

            int i = inputStream.read();

            if ( i == -1 )
            {
                return -1;
            }

            left--;
            return i;
        }

        public int read( byte b[], int off, int len )
            throws IOException
        {
            if ( left <= 0 )
            {
                return -1;
            }

            if ( len > left )
            {
                len = (int) left;
            }

            int i = inputStream.read( b, off, len );

            left -= i;

            return i;
        }

        public long skip( long n )
            throws IOException
        {
            throw new IOException( "Not supported" );
        }

        public int available()
            throws IOException
        {
            return (int) left;
        }

        public void close()
            throws IOException
        {
            // TODO: Make sure that we read out all the bytes from the underlying input stream
            if ( left != 0 )
            {
                ArUtil.skipBytes( inputStream, left );
            }

            // Read the extra pad byte if size is odd
            if ( size % 2 == 1 )
            {
                ArUtil.skipBytes( inputStream, 1 );
            }
        }

        public synchronized void mark( int readlimit )
        {
            throw new RuntimeException( "Not supported" );
        }

        public synchronized void reset()
            throws IOException
        {
            throw new RuntimeException( "Not supported" );
        }

        public boolean markSupported()
        {
            return false;
        }
    }
}
