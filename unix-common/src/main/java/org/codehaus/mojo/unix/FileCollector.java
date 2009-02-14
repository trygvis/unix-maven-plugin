package org.codehaus.mojo.unix;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface FileCollector
{
    FileObject getRoot();

    FileCollector addDirectory( RelativePath path, FileAttributes attributes )
        throws IOException;

    FileCollector addFile( FileObject fromFile, RelativePath toPath, FileAttributes attributes )
        throws IOException;
}
