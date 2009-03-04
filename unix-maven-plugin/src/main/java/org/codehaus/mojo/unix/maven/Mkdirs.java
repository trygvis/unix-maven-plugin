package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.core.CreateDirectoriesOperation;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Mkdirs
    extends AssemblyOp
{
    private String path;

    private String[] paths;

    private FileAttributes attributes = new FileAttributes();

    public Mkdirs()
    {
        super( "mkdirs" );
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public void setPaths( String[] paths )
    {
        this.paths = paths;
    }

    public void setAttributes( FileAttributes attributes )
    {
        this.attributes = attributes;
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        if ( path != null )
        {
            if ( paths != null )
            {
                throw new MojoFailureException(
                    "Only either 'path' or 'paths' can be set on a " + operationType + " operation." );
            }

            paths = new String[]{path};
        }

        return new CreateDirectoriesOperation( paths, applyDirectoryDefaults( defaults, attributes.create() ) );
    }
}
