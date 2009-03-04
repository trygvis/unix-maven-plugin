package org.codehaus.mojo.unix;

import fj.F;
import fj.data.Option;
import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface FileCollector
{
    // TODO: this probably shouldn't be here. replace with collect( FileObject root )?
    FileObject getRoot();

    FileCollector addDirectory( UnixFsObject.Directory directory )
        throws IOException;

    FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
        throws IOException;

    FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException;

    void applyOnFiles( F<RelativePath, Option<FileAttributes>> f );

    void applyOnDirectories( F<RelativePath, Option<FileAttributes>> f );
}
