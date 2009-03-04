package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.core.SymlinkOperation;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.UnixFileMode;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Symlink
    extends AssemblyOp
{
    private RelativePath source;

    private String target;

    public Symlink()
    {
        super( "symlink" );
    }

    public void setSource( String source )
    {
        this.source = fromString( source );
    }

    public void setTarget( String target )
    {
        this.target = target;
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        FileAttributes attributes = new FileAttributes( null, null, UnixFileMode._0777 );

        return new SymlinkOperation( source, target, applyFileDefaults( defaults, attributes.create() ) );
    }
}
