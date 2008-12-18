package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-dpkg-attached
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackageDpkgAttachedMojo
    extends AbstractPackageAttachedMojo
{
    /**
     * @parameter
     */
    private DpkgSpecificSettings dpkg;

    public PackageDpkgAttachedMojo()
    {
        super( "dpkg" );
    }

    protected MojoHelper getMojoHelper()
    {
        return super.getMojoHelper( new DpkgMojoHelper( dpkg ) );
    }
}
