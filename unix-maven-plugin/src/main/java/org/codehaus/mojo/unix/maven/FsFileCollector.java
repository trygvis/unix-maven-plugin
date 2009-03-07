package org.codehaus.mojo.unix.maven;

import fj.F;
import fj.Unit;
import fj.data.Option;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.Selectors;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.UnixUtil;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.asFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FsFileCollector
    implements FileCollector
{
    private final List<Callable> operations = new ArrayList<Callable>();

    private final FileObject fsRoot;

    private final FileObject root;

    public FsFileCollector( FileObject fsRoot )
        throws FileSystemException
    {
        this.fsRoot = fsRoot;
        FileSystemManager fileSystemManager = fsRoot.getFileSystem().getFileSystemManager();
        FileObject root = fileSystemManager.createVirtualFileSystem( fsRoot );
        root.createFolder();
        this.root = root;
    }

    public FileObject getFsRoot()
    {
        return fsRoot;
    }

    public FileObject getRoot()
    {
        return root;
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
    {
        operations.add( packageDirectory( directory.path ) );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        operations.add( packageFile( fromFile, file ) );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        operations.add( packageSymlink( symlink.path.string, fromString( symlink.target ) ) );

        return this;
    }

    public void applyOnFiles( F<RelativePath, Option<FileAttributes>> f )
    {
        // Not implemented
    }

    public void applyOnDirectories( F<RelativePath, Option<FileAttributes>> f )
    {
        // Not implemented
    }

    public void collect()
        throws Exception
    {
        for ( Callable operation : operations )
        {
            operation.call();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private Callable packageFile( final FileObject from, final UnixFsObject.RegularFile to )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                FileObject toFile = root.resolveFile( to.path.string );

                toFile.getParent().createFolder();
                toFile.copyFrom( from, Selectors.SELECT_SELF );
                toFile.getContent().setLastModifiedTime( to.lastModified.toDateTime().toDate().getTime() );
                return Unit.unit();
            }
        };
    }

    private Callable packageDirectory( final RelativePath path )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                root.resolveFile( path.string ).createFolder();
                return Unit.unit();
            }
        };
    }

    private Callable packageSymlink( final String source, final RelativePath target )
    {
        return new Callable()
        {
            public Object call()
                throws Exception
            {
                root.resolveFile( target.string ).getParent().createFolder();

                File file = asFile( fsRoot );

                UnixUtil.symlink( file, source, target );

                return Unit.unit();
            }
        };
    }
}
