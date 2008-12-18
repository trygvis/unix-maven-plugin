package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-rpm
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackageRpmMojo
    extends AbstractPackageMojo
{
    /**
     * @parameter
     */
    protected RpmSpecificSettings rpm;

    public PackageRpmMojo()
    {
        super( "rpm" );
    }

    protected MojoHelper getMojoHelper()
    {
        return new RpmMojoHelper( rpm );
    }
}
