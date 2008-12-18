package org.codehaus.mojo.unix.maven.util;

import org.apache.commons.vfs.FileObject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.AssemblyOperation;
import org.codehaus.mojo.unix.maven.Defaults;
import org.codehaus.mojo.unix.maven.Package;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility to handle the creation of a single package.
 * <p/>
 * Handles
 * <ul>
 * <li>Merging of values from the project vs what is configured per package</li>
 * </ul>
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageCreationUtil
{
    private final Package pakke;

    private final String classifier;

    private final MavenProject project;

    private final UnixPackage unixPackage;

    private final Map artifactMap;

    private final Defaults defaults;

    private List assemblyOperations = new ArrayList();

    public PackageCreationUtil( Package pakke, String classifier, MavenProject project, UnixPackage unixPackage,
                                Map artifactMap, Defaults defaults )
    {
        this.pakke = pakke;
        this.classifier = classifier;
        this.project = project;
        this.unixPackage = unixPackage;
        this.artifactMap = artifactMap;
        this.defaults = defaults;
    }

    // -----------------------------------------------------------------------
    // Setters that are similar to those un UnixPackage, but they select one
    // of the two the values.
    // -----------------------------------------------------------------------

    public PackageCreationUtil contact( String contact )
    {
        unixPackage.contact( contact );
        return this;
    }

    public PackageCreationUtil contactEmail( String contactEmail )
    {
        unixPackage.contactEmail( contactEmail );
        return this;
    }

    public PackageCreationUtil shortDescription( String packageName, String projectName )
        throws MissingSettingException
    {
        unixPackage.shortDescription( select( packageName, projectName ) );
        return this;
    }

    public PackageCreationUtil description( String packageDescription, String projectDescription )
        throws MissingSettingException
    {
        unixPackage.description( select( packageDescription, projectDescription ) );
        return this;
    }

    public PackageCreationUtil license( String license )
    {
        unixPackage.license( license );
        return this;
    }

    public PackageCreationUtil architecture( String projectArchitecture, String packageDefault )
        throws MissingSettingException
    {
        unixPackage.architecture( select( "architecture", projectArchitecture, packageDefault ) );
        return this;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public PackageCreationUtil appendAssemblyOperations( AssemblyOperation[] assemblyOperations )
    {
        if ( assemblyOperations == null )
        {
            return this;
        }
        this.assemblyOperations.addAll( Arrays.asList( assemblyOperations ) );

        return this;
    }

    public File createPackage()
        throws IOException, MissingSettingException, MojoExecutionException, MojoFailureException
    {
        // -----------------------------------------------------------------------
        // Assemble all the files
        // -----------------------------------------------------------------------

        assemble( assemblyOperations );

        // -----------------------------------------------------------------------
        // Package the stuff
        // -----------------------------------------------------------------------

        File packageFile = new File( project.getBuild().getDirectory(),
            project.getArtifactId() + ( classifier == null ? "" : "-" + pakke.getId() ) +
                "-" + unixPackage.getVersion().getMavenVersion() +
                "." + unixPackage.getPackageFileExtension() );

        unixPackage.
            name( project.getArtifactId() + ( classifier == null ? "" : "-" + classifier ) ).
            packageToFile( packageFile );

        return packageFile;
    }

    private void assemble( List assembly )
        throws MojoFailureException, MojoExecutionException
    {
        // A small hack to make sure that the toFile path is canonical
        FileCollector fileCollector = new FileCollector()
        {
            public FileCollector addDirectory( String path, String user, String group, String mode )
                throws IOException
            {
                unixPackage.addDirectory( new File( path ).getCanonicalPath(), user, group, mode );
                return this;
            }

            public FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
                throws IOException
            {
                if ( !fromFile.isReadable() )
                {
                    throw new RuntimeException( "File is not readable: " + fromFile.getName().getPath() );
                }

                unixPackage.addFile( fromFile, new File( toFile ).getCanonicalPath(), user, group, mode );
                return this;
            }
        };

        try
        {
            for ( Iterator it = assembly.iterator(); it.hasNext(); )
            {
                AssemblyOperation assemblyOperation = (AssemblyOperation) it.next();

                assemblyOperation.setArtifactMap( artifactMap );
                assemblyOperation.perform( defaults, fileCollector );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while assembling package.", e );
        }
    }

    private String select( String packageName, String projectName )
        throws MissingSettingException
    {
        if ( StringUtils.isNotEmpty( packageName ) )
        {
            return packageName;
        }

        return projectName;
    }

    private String select( String setting, String packageName, String projectName )
        throws MissingSettingException
    {
        if ( StringUtils.isNotEmpty( packageName ) )
        {
            return packageName;
        }

        if ( StringUtils.isNotEmpty( projectName ) )
        {
            return projectName;
        }

        throw new MissingSettingException( setting );
    }
}
