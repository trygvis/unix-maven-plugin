package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.pkg.PkgUnixPackage;
import org.codehaus.mojo.unix.maven.pkg.prototype.PrototypeEntry;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class PkgMojoHelper
    extends MojoHelper
{
    private PkgSpecificSettings pkg;

    public PkgMojoHelper( PkgSpecificSettings pkg )
    {
        this.pkg = pkg;
    }

    protected void validateMojoSettings()
    {
        if ( pkg == null )
        {
            pkg = new PkgSpecificSettings();
        }
    }

    protected void applyFormatSpecificSettingsToPackage( UnixPackage unixPackage )
    {
        // TODO: add the extra prototype lines
        PrototypeEntry[] extraPrototype = new PrototypeEntry[pkg.getExtraPrototype().length];

        for ( int i = 0; i < pkg.getExtraPrototype().length; i++ )
        {
            extraPrototype[i] = PrototypeEntry.fromLine( pkg.getExtraPrototype()[i] );
        }

        PkgUnixPackage.cast( unixPackage ).
            classes( pkg.getClasses() );
    }
}
