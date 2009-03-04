package org.codehaus.mojo.unix.core;

import static fj.data.Option.fromNull;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.UnixFsObject;
import static org.codehaus.mojo.unix.UnixFsObject.directory;
import static org.codehaus.mojo.unix.UnixFsObject.regularFile;
import org.codehaus.mojo.unix.util.RelativePath;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AssemblyOperation
{
    public abstract void perform( FileCollector fileCollector )
        throws IOException;

    public static UnixFsObject.RegularFile fromFileObject( RelativePath toFile, FileObject fromFile,
                                                           FileAttributes attributes )
        throws FileSystemException
    {
        FileContent content = fromFile.getContent();

        return regularFile( toFile,
                            new LocalDateTime( content.getLastModifiedTime() ),
                            content.getSize(),
                            fromNull( attributes ) );

    }

    public static  UnixFsObject.Directory dirFromFileObject( RelativePath toFile, FileObject fromFile,
                                                             FileAttributes attributes )
        throws FileSystemException
    {
        FileContent content = fromFile.getContent();

        return directory( toFile, new LocalDateTime( content.getLastModifiedTime() ), attributes );
    }
}
