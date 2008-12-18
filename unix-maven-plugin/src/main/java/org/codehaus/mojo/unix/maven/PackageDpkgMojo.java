package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-dpkg
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackageDpkgMojo
    extends AbstractPackageMojo
{
    /**
     * @parameter
     */
    private DpkgSpecificSettings dpkg;

    public PackageDpkgMojo()
    {
        super( "dpkg" );
    }

    protected MojoHelper getMojoHelper()
    {
        return new DpkgMojoHelper( dpkg );
    }
}
