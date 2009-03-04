package org.codehaus.mojo.unix.core;

import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyFileOperation
    extends AssemblyOperation
{
    private final FileAttributes attributes;

    private final FileObject fromFile;

    private final RelativePath toFile;

    public CopyFileOperation( FileAttributes attributes, FileObject fromFile, RelativePath toFile )
    {
        this.attributes = attributes;
        this.fromFile = fromFile;
        this.toFile = toFile;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        fileCollector.addFile( fromFile, fromFileObject( toFile, fromFile, attributes ) );
    }
}
