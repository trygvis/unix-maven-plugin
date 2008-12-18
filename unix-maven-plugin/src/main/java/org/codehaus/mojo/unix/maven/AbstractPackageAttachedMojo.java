package org.codehaus.mojo.unix.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPackageAttachedMojo
    extends AbstractUnixMojo
{
    protected final String formatType;

    protected AbstractPackageAttachedMojo( String formatType )
    {
        this.formatType = formatType;
    }

    protected abstract MojoHelper getMojoHelper();

    protected MojoHelper getMojoHelper( MojoHelper mojoHelper )
    {
        return mojoHelper.
            attachedMode();
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getMojoHelper().
            setMojoParameters( new PackagingMojoParameters(
                name,
                version,
                revision,
                description,
                contact,
                contactEmail,
                architecture,
                assembly,
                null ) ).
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
