package org.codehaus.mojo.unix.pkg;

import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgtransCommand
{
    private File basedir;

    private boolean debug;

    /**
     * -a: Use public key certificate associated with friendlyName alias, and the corresponding private key. See
     * KEYSTORE LOCATIONS and  KEYSTORE AND CERTIFICATE FORMATS in pkgadd(1M) for more information.
     */
    private String alias;

    /**
     * -g: Sign resulting datastream.
     */
    private boolean signDatastream;

    /**
     * -i: Copies only the pkginfo(4) and pkgmap(4) files.
     */
    private boolean copyOnlyPkginfoAndPkgmap;

    /**
     * -k: Use keystore to retrieve private key used to generate signature. If it not specified, default locations are
     * searched to find the specified private key specified by -a. If no alias is given, and multiple keys exist in the
     * key store, pkgtrans will abort. See KEYSTORE LOCATIONS and KEYSTORE AND CERTIFICATE FORMATS in pkgadd(1M) for
     * more information on search locations and formats.
     * <p/>
     * When running as a user other than root, the default base directory for certificate searching is ~/.pkg/security,
     * where ~ is the home directory of the user invoking pkgtrans.
     */
    private File keystore;

    /**
     * -n: Creates a new instance of the package on the destination device if any instance of this package already
     * exists, up to the number specified by the MAXINST variable in the pkginfo(4) file.
     */
    private boolean newInstance;

    /**
     * -o: Overwrites the same instance on the destination  device. Package instance will be overwritten if it
     * already exists.
     */
    private boolean overwrite;

    /**
     * -P: Supply password used to decrypt the keystore. See PASS PHRASE ARGUMENTS in pkgadd(1M) for details on the
     * syntax of the argument to this option.
     */
    private String password;

    /**
     * -s: Indicates that the package should be written to device2 as a datastream rather than as a file system. The
     * default behavior is to write a file system format on devices that support both formats.
     */
    private boolean asDatastream;

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public PkgtransCommand setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public PkgtransCommand setAlias( String alias )
    {
        this.alias = alias;
        return this;
    }

    public PkgtransCommand setSignDatastream( boolean signDatastream )
    {
        this.signDatastream = signDatastream;
        return this;
    }

    public PkgtransCommand setCopyOnlyPkginfoAndPkgmap( boolean copyOnlyPkginfoAndPkgmap )
    {
        this.copyOnlyPkginfoAndPkgmap = copyOnlyPkginfoAndPkgmap;
        return this;
    }

    public PkgtransCommand setKeystore( File keystore )
    {
        this.keystore = keystore;
        return this;
    }

    public PkgtransCommand setNewInstance( boolean newInstance )
    {
        this.newInstance = newInstance;
        return this;
    }

    public PkgtransCommand setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
        return this;
    }

    public PkgtransCommand setPassword( String password )
    {
        this.password = password;
        return this;
    }

    public PkgtransCommand setAsDatastream( boolean asDatastream )
    {
        this.asDatastream = asDatastream;
        return this;
    }

    /**
     * @see #execute(java.io.File, java.io.File, String)
     */
    public void execute( File device1, File device2 )
        throws IOException
    {
        execute( device1, device2, null );
    }

    /**
     * @param device1 Indicates the source device. The package or packages on this device will be translated and
     *                placed on device2.
     * @param device2 Indicates the destination device. Translated packages will be placed on this device.
     * @param pkginst Specifies which package instance or instances on device1 should be translated. The token all may
     *                be used to indicate all packages. pkginst.* can be used to indicate all instances of a package. If no packages
     *                are defined, a prompt shows all packages on the device and asks which to translate.
     * @throws IOException
     */
    public void execute( File device1, File device2, String pkginst )
        throws IOException
    {
        SystemCommand command = new SystemCommand().
            setCommand( "pkgtrans" ).
            setBasedir( basedir ).
            dumpOutputIf( debug );

        if ( StringUtils.isNotEmpty( alias ) )
        {
            command.addArgument( "-a" ).addArgument( alias );
        }

        if ( signDatastream )
        {
            command.addArgument( "-g" );
        }

        if ( copyOnlyPkginfoAndPkgmap )
        {
            command.addArgument( "-i" );
        }

        if ( keystore != null )
        {
            command.addArgument( "-k" ).addArgument( keystore.getAbsolutePath() );
        }

        if ( newInstance )
        {
            command.addArgument( "-n" );
        }

        if ( overwrite )
        {
            command.addArgument( "-o" );
        }

        if ( StringUtils.isNotEmpty( password ) )
        {
            command.addArgument( "-P" ).addArgument( password );
        }

        if ( asDatastream )
        {
            command.addArgument( "-s" );
        }

        if ( device1 == null || device2 == null )
        {
            throw new IOException( "Both device1 and device2 must be given." );
        }

        command.
            addArgument( device1.getAbsolutePath() ).
            addArgument( device2.getAbsolutePath() );

        if ( pkginst != null )
        {
            command.addArgument( pkginst );
        }

        command.
            execute().
            assertSuccess();
    }
}
