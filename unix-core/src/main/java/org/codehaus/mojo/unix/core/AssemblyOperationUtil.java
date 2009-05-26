package org.codehaus.mojo.unix.core;

import static fj.data.Option.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AssemblyOperationUtil
{
    public static UnixFsObject.RegularFile fromFileObject( RelativePath toFile, FileObject fromFile,
                                                           FileAttributes attributes )
        throws FileSystemException
    {
        FileContent content = fromFile.getContent();

        LocalDateTime time = new LocalDateTime( content.getLastModifiedTime() );

        return regularFile( toFile, time, content.getSize(), fromNull( attributes ) );
    }

    public static UnixFsObject.Directory dirFromFileObject( RelativePath toFile, FileObject fromFile,
                                                            FileAttributes attributes )
        throws FileSystemException
    {
        if ( !fromFile.getType().equals( FileType.FOLDER ) )
        {
            throw new FileSystemException( "Not a directory: " + fromFile.getName().getPath() + ", was: " +
                fromFile.getType() );
        }

        FileContent content = fromFile.getContent();

        return UnixFsObject.directory( toFile, new LocalDateTime( content.getLastModifiedTime() ), attributes );
    }
}
