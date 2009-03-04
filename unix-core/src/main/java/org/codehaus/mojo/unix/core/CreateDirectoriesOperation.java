package org.codehaus.mojo.unix.core;

import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.UnixFsObject;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CreateDirectoriesOperation
    extends AssemblyOperation
{
    private String[] paths;

    private FileAttributes attributes;

    public CreateDirectoriesOperation( String[] paths, FileAttributes attributes )
    {
        this.paths = paths;
        this.attributes = attributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        for (String path : paths)
        {
            fileCollector.addDirectory( UnixFsObject.directory( fromString( path ), new LocalDateTime(), attributes ) );
        }
    }
}
