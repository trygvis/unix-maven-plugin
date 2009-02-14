package org.codehaus.mojo.unix.maven;

import org.apache.maven.artifact.transform.SnapshotTransformation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractUnixMojo
    extends AbstractMojo
{
    /**
     * @parameter
     */
    protected AssemblyOperation[] assembly;

    /**
     * @parameter
     */
    protected Defaults defaults;

    /**
     * Optional parameter to specify the name of the package. If not set it will use the artifact id of the Maven
     * project.
     *
     * @parameter
     */
    protected String name;

    /**
     * Optional parameter to specify the version of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected String version;

    /**
     * Optional parameter to specify the revision of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected Integer revision;

    /**
     * @parameter expression="${project.description}"
     */
    protected String description;

    /**
     * @parameter
     * @required
     */
    protected String contact;

    /**
     * @parameter
     */
    protected String contactEmail;

    // TODO: Is this really common? Or just common enough?
    /**
     * @parameter
     */
    protected String architecture;

    /**
     * @parameter expression="${maven.unix.debug}"
     */
    protected boolean debug;

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    /**
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component role="org.codehaus.mojo.unix.maven.PackagingFormat"
     */
    protected Map formats;

    /**
     * @component role="org.apache.maven.artifact.transform.ArtifactTransformation" roleHint="snapshot"
     */
    protected SnapshotTransformation snapshotTransformation;

    /**
     * @component
     */
    protected MavenProjectHelper mavenProjectHelper;
}
