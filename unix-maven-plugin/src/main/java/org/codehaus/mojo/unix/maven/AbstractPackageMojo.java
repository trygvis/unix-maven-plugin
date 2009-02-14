package org.codehaus.mojo.unix.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPackageMojo
    extends AbstractUnixMojo
{
    /**
     * @parameter
     */
    protected Package[] packages;

    private final String formatType;

    protected AbstractPackageMojo( String formatType )
    {
        this.formatType = formatType;
    }

    protected abstract MojoHelper getMojoHelper();

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getMojoHelper().
            setMojoParameters( new PackagingMojoParameters( name,
                version,
                revision,
                description,
                contact,
                contactEmail,
                architecture,
                assembly,
                packages ) ).
            setup(
                formats,
                formatType,
                snapshotTransformation,
                project,
                mavenProjectHelper,
                debug,
                defaults ).execute();
    }
}
