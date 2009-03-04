package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.core.CopyDirectoryOperation;
import org.codehaus.mojo.unix.util.RelativePath;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Collections;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractFileSetOp
    extends AssemblyOp
{
    private RelativePath to = RelativePath.BASE;

    private List<String> includes = Collections.emptyList();

    private List<String> excludes = Collections.emptyList();

    private String pattern;

    private String replacement;

    private FileAttributes fileAttributes = new FileAttributes();

    private FileAttributes directoryAttributes = new FileAttributes();

    public AbstractFileSetOp( String operationType )
    {
        super( operationType );
    }

    public void setTo( String to )
    {
        this.to = RelativePath.fromString( to );
    }

    public void setIncludes( String[] includes )
    {
        this.includes = asList( includes );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = asList( excludes );
    }

    public void setPattern( String pattern )
    {
        this.pattern = nullifEmpty( pattern );
    }

    public void setReplacement( String replacement )
    {
        this.replacement = nullifEmpty( replacement );
    }

    public void setFileAttributes( FileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public void setDirectoryAttributes( FileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }

    protected AssemblyOperation createOperationInternal( FileObject archive, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        if ( pattern != null && replacement == null )
        {
            throw new MojoFailureException( "A replacement expression has to be set if a pattern is given." );
        }

        return new CopyDirectoryOperation( archive, to, includes, excludes, pattern, replacement,
                                           applyFileDefaults( defaults, fileAttributes.create() ),
                                           applyDirectoryDefaults( defaults, directoryAttributes.create() ) );
    }
}
