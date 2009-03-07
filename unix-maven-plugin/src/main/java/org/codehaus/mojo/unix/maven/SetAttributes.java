package org.codehaus.mojo.unix.maven;

import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.core.SetAttributesOperation;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributes
    extends AssemblyOp
{
    private RelativePath basedir;

    private List<String> includes = Collections.emptyList();

    private List<String> excludes = Collections.emptyList();

    private Option<FileAttributes> fileAttributes = none();

    private Option<FileAttributes> directoryAttributes = none();

    public SetAttributes()
    {
        super( "set-attributes" );
    }

    public void setBasedir( String basedir )
    {
        this.basedir = fromString( basedir );
    }

    public void setIncludes( String[] includes )
    {
        this.includes = asList( includes );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = asList( excludes );
    }

    public void setFileAttributes( FileAttributes fileAttributes )
    {
        this.fileAttributes = fromNull( fileAttributes );
    }

    public void setDirectoryAttributes( FileAttributes directoryAttributes )
    {
        this.directoryAttributes = fromNull( directoryAttributes );
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        return new SetAttributesOperation( this.basedir, includes, excludes,
                                           fileAttributes.map( FileAttributes.create_ ),
                                           directoryAttributes.map( FileAttributes.create_ ) );
    }
}
