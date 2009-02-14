package org.codehaus.mojo.unix.ar;

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class ArWriter
{
    private OutputStream output;

    static Charset charset;

    public static final String BLANKS = "               ";

    static
    {
        charset = Charset.forName( ArUtil.US_ASCII );
    }

    public ArWriter( File file )
        throws IOException
    {
        output = new FileOutputStream( file );

        output.write( toBytes( ArUtil.AR_ARCHIVE_MAGIC ) );
        output.flush();
    }

    public void add( ArFile arFile )
        throws IOException
    {
        output.write( toBytes( arFile.name, 16 ) );
        output.write( toBytes( Long.toString( arFile.lastModified ), 12 ) );
        output.write( toBytes( Integer.toString( arFile.ownerId ), 6 ) );
        output.write( toBytes( Integer.toString( arFile.groupId ), 6 ) );
        output.write( toBytes( Integer.toOctalString( arFile.mode ), 8 ) );
        output.write( toBytes( Long.toString( arFile.size ), 10 ) );
        output.write( toBytes( ArUtil.AR_FILE_MAGIC ) );
        output.flush();

        InputStream is = null;
        try
        {
            is = new FileInputStream( arFile.file );
            ArUtil.copy( is, output, 8192 );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    private byte[] toBytes( String value )
    {
        return toBytes( value, value.length() );
    }

    private byte[] toBytes( String value, int size )
    {
        String s = value;

        if ( s.length() > size )
        {
            throw new RuntimeException( "Internal error. Field size (" + s.length() + ") > max size (" + size + ")" );
        }

        if ( s.length() < size )
        {
            s += BLANKS.substring( 0, size - s.length() );
        }

        ByteBuffer byteBuffer = charset.encode( s );
        return byteBuffer.array();
    }

    public void close()
        throws IOException
    {
        output.close();
    }
}
