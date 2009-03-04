package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LineStreamUtil
{
    public static Iterator<String> prefix( final Iterator<String> lines, final String prefix )
    {
        return new Iterator<String>()
        {
            public boolean hasNext()
            {
                return lines.hasNext();
            }

            public String next()
            {
                return prefix + lines.next();
            }

            public void remove()
            {
                lines.remove();
            }
        };
    }

    public static Iterable<String> prefix( final Iterable<String> lines, final String prefix )
    {
        return new Iterable<String>()
        {
            public Iterator<String> iterator()
            {
                return prefix( lines.iterator(), prefix );
            }
        };
    }

    // -----------------------------------------------------------------------
    // LineProducer
    // -----------------------------------------------------------------------

    public static Iterator<String> toIterator( LineProducer lineProducer )
    {
        LineFile lines = new LineFile();
        lineProducer.streamTo( lines );
        return lines.iterator();
    }

    public static Iterator<String> toIterator( Iterable<LineProducer> lineProducers )
    {
        return toIterator( lineProducers.iterator() );
    }

    public static Iterator<String> toIterator( final Iterator<LineProducer> lineProducers )
    {
        if ( lineProducers == null || !lineProducers.hasNext() )
        {
            return Collections.<String>emptyList().iterator();
        }

        return new Iterator<String>()
        {
            Iterator<String> lines;

            public boolean hasNext()
            {
                return lines != null || lineProducers.hasNext();
            }

            public String next()
            {
                if ( lines == null )
                {
                    LineFile file = new LineFile();
                    lineProducers.next().streamTo( file );
                    lines = file.iterator();
                }

                return lines.next();
            }

            public void remove()
            {
                throw new RuntimeException( "Not implemented" );
            }
        };
    }

    public static void toFile( LineProducer lineProducer, File file )
        throws IOException
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter( file );
            LineWriterWriter writer = new LineWriterWriter( fileWriter );
            lineProducer.streamTo( writer );
            writer.close();
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }
}
