package org.codehaus.mojo.unix.util.vfs;

import org.apache.commons.vfs.FileObject;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VfsUtil
{
    public static File asFile( FileObject fileObject )
    {
        return new File( fileObject.getName().getPath() );
    }
}
