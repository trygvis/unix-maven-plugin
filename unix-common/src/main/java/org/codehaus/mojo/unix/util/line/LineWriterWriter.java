package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.IOUtil;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * A lazy writer that won't throw any exceptions until close() is called. If there is an exception while writing,
 * it will be stored and throw later on. If an exception is thrown while streaming <em>and</em> when the stream is
 * closed, the exception that was caught during streaming will be thrown from <code>close()</code>. The stream will
 * still be closed.
 *
 * There is no flushing during streaming. 
 */
public class LineWriterWriter
    extends AbstractLineStreamWriter
    implements Flushable, Closeable
{
    private final Writer writer;

    private IOException e;

    public LineWriterWriter( Writer writer )
    {
        this.writer = writer;
    }

    protected void onLine( String line )
    {
        if ( e != null )
        {
            return;
        }

        try
        {
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
        try
        {
            if ( e != null )
            {
                throw e;
            }
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    public void flush()
        throws IOException
    {
        try
        {
            writer.flush();
        }
        catch ( IOException e )
        {
            this.e = e;
        }
    }
}
