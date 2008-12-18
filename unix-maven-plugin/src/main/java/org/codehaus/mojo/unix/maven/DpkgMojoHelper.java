package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.dpkg.DpkgUnixPackage;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class DpkgMojoHelper
    extends MojoHelper
{
    private DpkgSpecificSettings dpkg;

    public DpkgMojoHelper( DpkgSpecificSettings dpkg )
    {
        this.dpkg = dpkg;
    }

    protected void validateSettings()
        throws MissingSettingException
    {
        if ( dpkg == null )
        {
            throw new MissingSettingException( "You need to specify the required properties when building dpkg packages." );
        }

        if ( StringUtils.isEmpty( dpkg.getPriority() ) )
        {
            dpkg.setPriority( "standard" );
        }

        if ( StringUtils.isEmpty( dpkg.getSection() ) )
        {
            throw new MissingSettingException( "Section has to be specified." );
        }
    }

    protected void customizeMojoParameters( PackagingMojoParameters mojoParameters )
    {

    }

    protected void customizePackage( UnixPackage unixPackage )
    {
        DpkgUnixPackage.cast( unixPackage ).
            priority( dpkg.getPriority() ).
            section( dpkg.getSection() );
    }
}
