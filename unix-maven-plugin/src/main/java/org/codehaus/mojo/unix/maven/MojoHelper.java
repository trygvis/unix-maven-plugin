package org.codehaus.mojo.unix.maven;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.transform.SnapshotTransformation;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.util.PackageCreationUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class MojoHelper
{
    private String formatType;
    private MavenProject project;
    private MavenProjectHelper mavenProjectHelper;
    private Defaults defaults;
    private boolean debug;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private boolean attachedMode;

    private File buildDirectory;

    private PackagingFormat format;

    private PackageVersion version;

    private Map<String, Artifact> artifactMap;

    private PackagingMojoParameters mojoParameters;

    // -----------------------------------------------------------------------
    // This can be overridden
    // -----------------------------------------------------------------------

    protected abstract void validateMojoSettings()
        throws MissingSettingException;

    protected abstract void applyFormatSpecificSettingsToPackage( UnixPackage unixPackage );

    // -----------------------------------------------------------------------
    // The shit
    // -----------------------------------------------------------------------

    public MojoHelper attachedMode()
    {
        attachedMode = true;
        return this;
    }

    /**
     * All the parameters that are supposed to be customized by a Mojo user.
     */
    public MojoHelper setMojoParameters( PackagingMojoParameters mojoParameters )
        throws MojoFailureException
    {
        this.mojoParameters = mojoParameters;

        mojoParameters.packages = validatePackages( mojoParameters.packages );

        return this;
    }

    public final MojoHelper setup( Map formats,
                                   String formatType,
                                   SnapshotTransformation snapshotTransformation,
                                   MavenProject project,
                                   MavenProjectHelper mavenProjectHelper,
                                   boolean debug,
                                   Defaults defaults )
        throws MojoFailureException
    {
        this.formatType = formatType;
        this.project = project;
        this.mavenProjectHelper = mavenProjectHelper;
        this.debug = debug;
        this.defaults = defaults != null ? defaults : new Defaults();

        artifactMap = new HashMap<String, Artifact>();

        //noinspection unchecked
        for ( Artifact artifact : (Set<Artifact>) project.getArtifacts() )
        {
            artifactMap.put( artifact.getDependencyConflictId(), artifact );
        }

        format = (PackagingFormat) formats.get( formatType );

        if ( format == null )
        {
            throw new MojoFailureException( "Internal error, could not find format for type '" + formatType + "'." );
        }

        // TODO: This is using a private Maven API that might change. Perhaps use some reflection magic here.
        String timestamp = snapshotTransformation.getDeploymentTimestamp();

        buildDirectory = new File( project.getBuild().getDirectory() );

        version = PackageVersion.create( project.getVersion(), timestamp, project.getArtifact().isSnapshot(),
            mojoParameters.version, mojoParameters.revision );

        return this;
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        FileObject buildDirectory;
        FileObject basedir;

        try
        {
            FileSystemManager fileSystemManager = VFS.getManager();
            basedir = fileSystemManager.resolveFile( this.project.getBasedir().getAbsolutePath() );
            buildDirectory = fileSystemManager.resolveFile( this.buildDirectory.getAbsolutePath() );
        }
        catch ( FileSystemException e )
        {
            throw new MojoExecutionException( "Error while initializing Commons VFS", e);
        }

        // -----------------------------------------------------------------------
        // Create each package
        // -----------------------------------------------------------------------

        for ( int i = 0; i < mojoParameters.packages.length; i++ )
        {
            Package pakke = mojoParameters.packages[i];

            try
            {
                String classifier = pakke.getId().equals( "default" ) ? null : pakke.getId();

                String name = "unix/root-" + formatType + "-" + pakke.getId();

                FileObject packageRoot = buildDirectory.resolveFile( name );
                packageRoot.createFolder();

                UnixPackage unixPackage = format.start().
                    basedir( project.getBasedir() ).
                    workingDirectory( packageRoot ).
                    mavenCoordinates( project.getGroupId(), project.getArtifactId(), classifier ).
                    version( version ).
                    debug( debug );

                // -----------------------------------------------------------------------
                // Let the implementation add its metadata
                // -----------------------------------------------------------------------

                validateMojoSettings();

                applyFormatSpecificSettingsToPackage( unixPackage );

                // -----------------------------------------------------------------------
                // DO IT
                // -----------------------------------------------------------------------

                // TODO: here the logic should be different if many packages are to be created.
                // Example: packageName should be taken from mojoParameters if there is only a single package, if not
                //          it should come from the Pakke object. This should also be validated, at least for
                //          packageName

                File packageFile = new PackageCreationUtil( pakke, classifier, project, unixPackage, artifactMap,
                                                            defaults ).
                    appendAssemblyOperations( mojoParameters.assembly ).
                    appendAssemblyOperations( pakke.getAssembly() ).
                    name( pakke.getPackageName(), project.getArtifactId() + ( classifier == null ? "" : "-" + classifier ) ).
                    contact( mojoParameters.contact ).
                    contactEmail( mojoParameters.contactEmail ).
                    shortDescription( pakke.getName(), project.getName() ).
                    description( pakke.getDescription(), project.getDescription() ).
                    license( getLicense( format, project ) ).
                    architecture( mojoParameters.architecture, format.defaultArchitecture() ).
                    createPackage( basedir );

                attach( classifier, unixPackage, packageFile );
            }
            catch ( MissingSettingException e )
            {
                throw new MojoFailureException(
                    "Missing required setting '" + e.getSetting() + "' for '" + pakke.getId() + "', format '" + formatType + "'." );
            }
            catch ( MojoExecutionException e )
            {
                throw e;
            }
            catch ( MojoFailureException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Unable to create package.", e );
            }
        }
    }

    private void attach( String classifier, UnixPackage unixPackage, File packageFile )
    {
        if ( attachedMode )
        {
            mavenProjectHelper.attachArtifact( project, unixPackage.getPackageFileExtension(),
                classifier, packageFile );
        }
        else
        {
            if ( classifier == null )
            {
                project.getArtifact().setFile( packageFile );
            }
            else
            {
                mavenProjectHelper.attachArtifact( project, formatType, classifier, packageFile );
            }
        }
    }

    private Package[] validatePackages( Package[] packages )
        throws MojoFailureException
    {
        if ( packages == null )
        {
            packages = new Package[1];
        }

        if ( packages[0] == null )
        {
            packages[0] = new Package();
        }

        Set<String> names = new HashSet<String>();

        for (Package pakke : packages)
        {
            if ( StringUtils.isEmpty( pakke.getId() ) )
            {
                pakke.setId( "default" );
            }

            if ( names.contains( pakke.getId() ) )
            {
                throw new MojoFailureException( "Duplicate package id: '" + pakke.getId() + "'." );
            }

            names.add( pakke.getId() );
        }

        return packages;
    }

    private static String getLicense( PackagingFormat format, MavenProject project )
    {
        if ( project.getLicenses().size() == 0 )
        {
            if ( format.licenseRequired() )
            {
                throw new RuntimeException( "At least one license is required" );
            }

            return null;
        }

        return ( (License) project.getLicenses().get( 0 ) ).getName();
    }

    protected static String defaultValue( String value, String defaultValue )
    {
        return StringUtils.isNotEmpty( value ) ? value : defaultValue;
    }
}
