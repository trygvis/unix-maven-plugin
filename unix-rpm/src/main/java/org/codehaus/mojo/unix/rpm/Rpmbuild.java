package org.codehaus.mojo.unix.rpm;

import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Rpmbuild
{
    private boolean debug;

    private File buildroot;

    private File specFile;

    private String rpmbuildPath = "rpmbuild";

    private List defines = new ArrayList();

    public Rpmbuild setDebug( boolean debug )
    {
        this.debug = debug;

        return this;
    }

    public Rpmbuild setBuildroot( File buildroot )
    {
        this.buildroot = buildroot;
        return this;
    }

    public Rpmbuild setSpecFile( File specFile )
    {
        this.specFile = specFile;
        return this;
    }

    public Rpmbuild setRpmbuildPath( String rpmbuildPath )
    {
        this.rpmbuildPath = StringUtils.defaultString( rpmbuildPath, this.rpmbuildPath );
        return this;
    }

    public Rpmbuild define( String define )
    {
        this.defines.add( define );
        return this;
    }

    public void buildBinary()
        throws IOException
    {
        if ( specFile == null )
        {
            throw new IOException( "Package specFile is not set." );
        }

        SystemCommand command = new SystemCommand().setBasedir( new File( "/" ) );

        // TODO: Only the _topdir defines should be there, the others should be in the spec file
        // TODO: This should be configurable
        command.
            dumpCommandIf( debug ).
            dumpOutputIf( debug ).
            setCommand( rpmbuildPath ).
            addArgument( "-bb" ).
            addArgument( "--buildroot" ).
            addArgument( buildroot.getAbsolutePath() ).
            addArgument( "--target" ).
            addArgument( "noarch" ).
            addArgument( specFile.getAbsolutePath() );

        for ( Iterator it = defines.iterator(); it.hasNext(); )
        {
            command.
                addArgument( "--define" ).
                addArgument( it.next().toString() );
        }

        command.
            execute().
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "rpmbuild" );
    }
}
