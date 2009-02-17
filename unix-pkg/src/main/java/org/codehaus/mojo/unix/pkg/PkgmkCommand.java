package org.codehaus.mojo.unix.pkg;

import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgmkCommand
{
    private File basedir = new File( "/" );

    private boolean debug;

    /**
     * Overrides the architecture information provided in the pkginfo(4) file with arch.
     */
    private String arch;

    /**
     * Prepends the indicated base_src_dir to locate relocatable objects on the source machine. Use this option
     * to search for all objects in the prototype file. pkgmk expects to find the objects in /base_src_dir or to
     * locate the objects by use of the -b and -r options, respectively.
     */
    private String baseSrcDir;

    /**
     * Creates the package on device. device can be an absolute directory pathname or the identifiers for a floppy
     * disk or removable disk (for example, /dev/diskette). The default device is the installation spool directory
     * (/var/spool/pkg).
     */
    private File device;

    /**
     * Uses the file prototype as input to the command. The default prototype filename is [Pp]rototype.
     */
    private File prototype;

    /**
     * Specifies the maximum size in 512 byte blocks of the output device as limit. By default, if the output file is
     * a directory or a mountable device, pkgmk employs the df(1M) command to dynamically calculate the amount of
     * available space on the output device. This option is useful in conjunction with pkgtrans(1) to create a
     * package with a datastream format.
     */
    private String limit;

    /**
     * Overwrites the same instance; package instance is overwritten if it already exists.
     */
    private boolean overwrite;

    /**
     * Overrides the production stamp definition in the pkginfo(4) file with pstamp.
     */
    private String pstamp;

    /**
     * Uses the indicated root_path with the source pathname appended to locate objects on the source machine, using
     * a comma (,) as the separator for the path elements. If this option is specified, look for the full destination
     * path in each of the directories specified. If neither -b nor -r is specified, look for the leaf filename in the
     * current directory.
     */
    private File rootPath;

    /**
     * Overrides the version information provided in the pkginfo(4) file with version.
     */
    private String version;

    /**
     * Places the indicated variable in the packaging environment. (See prototype(4) for definitions of variable
     * specifications.)
     */
    private List variables = new ArrayList();

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public PkgmkCommand setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public PkgmkCommand setArch( String arch )
    {
        this.arch = arch;
        return this;
    }

    public PkgmkCommand setBaseSrcDir( String baseSrcDir )
    {
        this.baseSrcDir = baseSrcDir;
        return this;
    }

    public PkgmkCommand setDevice( File device )
    {
        this.device = device;
        return this;
    }

    public PkgmkCommand setPrototype( File prototype )
    {
        this.prototype = prototype;
        return this;
    }

    public PkgmkCommand setLimit( String limit )
    {
        this.limit = limit;
        return this;
    }

    public PkgmkCommand setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
        return this;
    }

    public PkgmkCommand setPstamp( String pstamp )
    {
        this.pstamp = pstamp;
        return this;
    }

    public PkgmkCommand setRootPath( File rootPath )
    {
        this.rootPath = rootPath;
        return this;
    }

    public PkgmkCommand setVersion( String version )
    {
        this.version = version;
        return this;
    }

    public PkgmkCommand addVariables( String key, String value )
    {
        variables.add( key + "=" + value );
        return this;
    }

    public void execute()
        throws IOException
    {
        // Seems like pkgmk doesn't like its stderr/stdout to be closed.
        SystemCommand.StringBufferLineConsumer out = new SystemCommand.StringBufferLineConsumer();

        SystemCommand command = new SystemCommand().
            setCommand( "pkgmk" ).
            setBasedir( basedir ).
            dumpCommandIf( debug ).
            withStderrConsumer( out ).
            withStdoutConsumer( out );

        if ( StringUtils.isNotEmpty( arch ) )
        {
            command.addArgument( "-a" ).addArgument( arch );
        }

        if ( StringUtils.isNotEmpty( baseSrcDir ) )
        {
            command.addArgument( "-b" ).addArgument( baseSrcDir );
        }

        if ( device != null )
        {
            command.addArgument( "-d" ).addArgument( device.getAbsolutePath() );
        }

        if ( prototype != null )
        {
            command.addArgument( "-f" ).addArgument( prototype.getAbsolutePath() );
        }

        if ( StringUtils.isNotEmpty( limit ) )
        {
            command.addArgument( "-l" ).addArgument( limit );
        }

        if ( overwrite )
        {
            command.addArgument( "-o" );
        }

        if ( StringUtils.isNotEmpty( pstamp ) )
        {
            command.addArgument( "-p" ).addArgument( pstamp );
        }

        if ( rootPath != null )
        {
            command.addArgument( "-r" ).addArgument( rootPath.getAbsolutePath() );
        }

        if ( StringUtils.isNotEmpty( version ) )
        {
            command.addArgument( "-v" ).addArgument( version );
        }

        SystemCommand.ExecutionResult result = command.
                addArguments(variables).
                execute();

        if ( debug )
        {
            System.out.println( "------------------------------------------------------" );
            System.out.println( result.command + " output:" );
            System.out.println( out );
        }

        result.
                assertSuccess();
    }
}
