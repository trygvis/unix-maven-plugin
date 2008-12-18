package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-pkg-attached
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackagePkgAttachedMojo
    extends AbstractPackageAttachedMojo
{
    /**
     * @parameter
     */
    private PkgSpecificSettings pkg;

    public PackagePkgAttachedMojo()
    {
        super( "pkg" );
    }

    protected MojoHelper getMojoHelper()
    {
        return super.getMojoHelper( new PkgMojoHelper( pkg ) );
    }
}
