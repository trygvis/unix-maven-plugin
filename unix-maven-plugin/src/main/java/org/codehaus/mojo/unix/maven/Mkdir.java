package org.codehaus.mojo.unix.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Mkdir
    extends AssemblyOperation
{
    private String path;

    private FileAttributes attributes = new FileAttributes();

    public Mkdir()
    {
        super( "mkdir" );
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public void setAttributes( FileAttributes attributes )
    {
        this.attributes = attributes;
    }

    public void perform( FileObject basedir, Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException
    {
        validateIsSet( path, "path" );

        org.codehaus.mojo.unix.FileAttributes attributes =
            Defaults.DEFAULT_DIRECTORY_ATTRIBUTES.
                useAsDefaultsFor( defaults.getDirectoryAttributes() ).
                    useAsDefaultsFor( this.attributes.create() );

        fileCollector.addDirectory( RelativePath.fromString( path ), attributes );
    }
}
