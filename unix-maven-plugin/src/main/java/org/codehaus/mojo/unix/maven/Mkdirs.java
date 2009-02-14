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
public class Mkdirs
    extends AssemblyOperation
{
    private String[] paths;

    private FileAttributes attributes = new FileAttributes();

    public Mkdirs()
    {
        super( "mkdirs" );
    }

    public void setPaths( String[] paths )
    {
        this.paths = paths;
    }

    public void setAttributes( FileAttributes attributes )
    {
        this.attributes = attributes;
    }

    public void perform( FileObject basedir, Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException
    {
        validateIsSet( paths, "paths" );

        org.codehaus.mojo.unix.FileAttributes attributes =
            Defaults.DEFAULT_DIRECTORY_ATTRIBUTES.
                useAsDefaultsFor( defaults.getDirectoryAttributes() ).
                    useAsDefaultsFor( this.attributes.create() );

        for ( int i = 0; i < paths.length; i++ )
        {
            fileCollector.addDirectory( RelativePath.fromString( paths[i] ), attributes );
        }
    }
}
