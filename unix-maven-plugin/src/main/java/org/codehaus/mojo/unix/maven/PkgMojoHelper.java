package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.pkg.PkgUnixPackage;
import org.codehaus.mojo.unix.maven.pkg.prototype.AbstractPrototypeEntry;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class PkgMojoHelper
    extends MojoHelper
{
    private PkgSpecificSettings pkg;

    private AbstractPrototypeEntry[] extraPrototype;

    public PkgMojoHelper( PkgSpecificSettings pkg )
    {
        this.pkg = pkg;
    }

    protected void validateSettings()
    {
        if ( pkg == null )
        {
            pkg = new PkgSpecificSettings();
        }
    }

    protected void customizeMojoParameters( PackagingMojoParameters mojoParameters )
    {
    }

    protected void customizePackage( UnixPackage unixPackage )
    {
        extraPrototype = new AbstractPrototypeEntry[pkg.getExtraPrototype().length];

        for ( int i = 0; i < pkg.getExtraPrototype().length; i++ )
        {
            extraPrototype[i] = AbstractPrototypeEntry.fromLine( pkg.getExtraPrototype()[i] );

            System.out.println( "extraPrototype[i] = " + extraPrototype[i] );
        }

        PkgUnixPackage.cast( unixPackage ).
            classes( pkg.getClasses() );
    }
}
