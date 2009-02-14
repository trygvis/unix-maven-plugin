package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LineStreamUtil
{
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
