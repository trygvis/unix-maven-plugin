package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.core.CopyFileOperation;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyArtifact
    extends AssemblyOp
{
    private String artifact;

    private RelativePath toFile;

    private RelativePath toDir;

    private FileAttributes attributes = new FileAttributes();

    public CopyArtifact()
    {
        super( "copy-artifact" );
    }

    public void setArtifact( String artifact )
    {
        this.artifact = nullifEmpty( artifact );
    }

    public void setToFile( String toFile )
    {
        this.toFile = fromString( toFile );
    }

    public void setToDir( String toDir )
    {
        this.toDir = fromString( toDir );
    }

    public void setAttributes( FileAttributes attributes )
    {
        this.attributes = attributes;
    }

    public AssemblyOperation createOperation( FileObject basedir, Defaults defaults )
        throws MojoFailureException, FileSystemException
    {
        File artifactFile = validateArtifact( artifact );

        RelativePath toFile = validateAndResolveOutputFile( artifactFile, toDir, this.toFile );

        return new CopyFileOperation( applyFileDefaults( defaults, attributes.create() ),
                                      resolve( basedir.getFileSystem(), artifactFile ),
                                      toFile );
    }
}
