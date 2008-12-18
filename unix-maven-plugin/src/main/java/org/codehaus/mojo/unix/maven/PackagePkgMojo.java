package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-pkg
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackagePkgMojo
    extends AbstractPackageMojo
{
    /**
     * @parameter
     */
    private PkgSpecificSettings pkg;

    public PackagePkgMojo()
    {
        super( "pkg" );
    }

    protected MojoHelper getMojoHelper()
    {
        return new PkgMojoHelper( pkg );
    }
}
