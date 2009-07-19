package org.codehaus.mojo.unix.util.line;

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

import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;
import static java.util.Collections.*;

/**
 * TODO: Implement equals and hashCode.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LineFile
    extends AbstractLineStreamWriter
    implements Iterable<String>
{
    private final List<String> lines = new ArrayList<String>( 1000 );

    protected void onLine( String line )
    {
        lines.add( line != null ? line : "" );
    }

    public List<String> getLines()
    {
        return unmodifiableList( lines );
    }

    public int size()
    {
        return lines.size();
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

    public static LineFile fromList( fj.data.List<String> lines )
    {
        LineFile lineFile = new LineFile();
        lineFile.lines.addAll( lines.toCollection() );
        return lineFile;
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
