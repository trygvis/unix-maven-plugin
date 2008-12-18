package org.codehaus.mojo.unix.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Mkdirs
    extends AssemblyOperation
{
    private String[] paths;

    private String user;

    private String group;

    private String mode;

    public Mkdirs()
    {
        super( "mkdirs" );
    }

    public void setPaths( String[] paths )
    {
        this.paths = paths;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public void setMode( String mode )
    {
        this.mode = mode;
    }

    public void perform( Defaults defaults, FileCollector fileCollector )
        throws MojoFailureException, IOException
    {
        validateIsSet( paths, "paths" );

        user = StringUtils.isNotEmpty( user ) ? user : defaults.getDirectoryUser();
        group = StringUtils.isNotEmpty( group ) ? group : defaults.getDirectoryGroup();
        mode = StringUtils.isNotEmpty( mode ) ? mode : defaults.getDirectoryMode();

        for ( int i = 0; i < paths.length; i++ )
        {
            fileCollector.addDirectory( paths[i], user, group, mode );
        }
    }
}
