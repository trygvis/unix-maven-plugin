package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.Writer;

/**
 * A lazy writer that won't throw any exceptions until close() is called. If there is an exception while writing,
 * it will be stored and throw later on.
 */
public class LineWriterWriter
    extends AbstractLineStreamWriter
{
    private Writer writer;

    private IOException e;

    public LineWriterWriter( Writer writer )
    {
        this.writer = writer;
    }

    protected void onLine( String prefix, String line )
    {
        if ( e != null )
        {
            return;
        }

        try
        {
            if ( prefix != null )
            {
                writer.write( prefix );
            }
            if ( line != null )
            {
                writer.write( line );
            }
            writer.write( EOL );
        }
        catch ( IOException e )
        {
            this.e = e;
        }
    }

    public void close()
        throws IOException
    {
        IOUtil.close( writer );

        if ( e != null )
        {
            throw e;
        }
    }
}
