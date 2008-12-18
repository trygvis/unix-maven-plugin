package org.codehaus.mojo.unix.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Executes "dpkg-scanpackages".
 * <p/>
 * By default the entire local repository will be scanned.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal dpkg-scanpackages
 */
public class DpkgScanPackagesMojo
    extends AbstractMojo
{
    /**
     * The base of where to scan. If not set, it will default to the entire local repository.
     *
     * @parameter expression="${maven.unix.dpkg.scanpackages.root}"
     */
    private File root;

    /**
     * @parameter expression="${maven.unix.dpkg.scanpackages.prefix}"
     */
    private String pathPrefix;

    /**
     * @parameter expression="${maven.unix.dpkg.scanpackages.override}" default-value="/dev/null"
     */
    private String overrideFile;

    /**
     * @parameter expression="${maven.unix.dpkg.scanpackages.multiversion}" default-value="true"
     */
    private boolean multiversion;

    /**
     * @parameter expression="${maven.unix.debug}" default-value="false"
     */
    protected boolean debug;

    /**
     * @parameter expression="${maven.unix.dpkg.scanpackages.output}" default-value="target/Packages"
     */
    protected File outputFile;

    /**
     * @parameter expression="${settings.localRepository}"
     * @readonly
     */
    private String localRepository;

    /**
     * @parameter expression="${basedir}"
     * @readonly
     */
    private File basedir;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( root == null )
        {
            root = new File( localRepository );
        }

        if ( !outputFile.isAbsolute() )
        {
            outputFile = new File( basedir, outputFile.getPath() );
        }

        if ( !outputFile.getParentFile().isDirectory() )
        {
            if ( !outputFile.getParentFile().mkdirs() )
            {
                throw new MojoFailureException( "Could not create parent directories for package file: " + outputFile.getAbsolutePath() );
            }
        }

        OutputStream output = null;

        try
        {
            output = new FileOutputStream( outputFile );

            new SystemCommand().
                dumpCommandIf( debug ).
                withStdoutConsumer( output ).
                withStderrConsumer( System.out ).
                setCommand( "dpkg-scanpackages" ).
                setBasedir( root ).
                addArgumentIf( multiversion, "-m" ).
                addArgument( "." ).
                addArgument( overrideFile ).
                addArgumentIfNotEmpty( pathPrefix ).
                execute().
                assertSuccess();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not run dpkg-scanpackages.", e );
        }
        finally {
            IOUtil.close(output);
        }
    }
}
