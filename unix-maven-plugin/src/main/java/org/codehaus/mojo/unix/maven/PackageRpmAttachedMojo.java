package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageBinaryMojo.java 7201 2008-07-02 11:53:19Z trygvis $
 * @goal package-rpm-attached
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class PackageRpmAttachedMojo
    extends AbstractPackageAttachedMojo
{
    /**
     * @parameter
     */
    protected RpmSpecificSettings rpm;

    public PackageRpmAttachedMojo()
    {
        super( "rpm" );
    }

    protected MojoHelper getMojoHelper()
    {
        return super.getMojoHelper( new RpmMojoHelper( rpm ) );
    }
}
