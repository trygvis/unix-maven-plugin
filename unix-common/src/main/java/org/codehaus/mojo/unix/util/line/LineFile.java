package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.Iterator;
import java.util.List;
/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LineFile
    extends AbstractLineStreamWriter
    implements Iterable<String>
{
    private List<String> lines = new ArrayList<String>( 1000 );

    protected void onLine( String prefix, String line )
    {
        if ( prefix != null )
        {
            lines.add( prefix + line );
        }
        else
        {
            lines.add( line != null ? line : "" );
        }
    }

    public List<String> getLines()
    {
        return unmodifiableList( lines );
    }

    public Iterator<String> iterator()
    {
        return unmodifiableList( new ArrayList<String>( lines ) ).iterator();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for (String line : lines)
        {
            buffer.append( line ).append( EOL );
        }
        return buffer.toString();
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static LineFile fromFile( File file )
        throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new FileReader( file ) );

            LineFile lineFile = new LineFile();
            String line = reader.readLine();

            while ( line != null )
            {
                lineFile.lines.add( line );
                line = reader.readLine();
            }

            return lineFile;
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    public void writeTo( File file )
        throws IOException
    {
        LineWriterWriter writer = null;
        try
        {
            writer = new LineWriterWriter( new FileWriter( file ) );

            for (String line : lines)
            {
                writer.add( line);
            }
        }
        finally
        {
            if ( writer != null )
            {
                writer.close();
            }
        }
    }
}
