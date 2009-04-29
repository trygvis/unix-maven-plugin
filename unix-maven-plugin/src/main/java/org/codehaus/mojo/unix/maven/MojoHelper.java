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

import fj.*;
import fj.data.List;
import static fj.data.List.*;
import static fj.data.List.single;
import fj.data.*;
import fj.data.Set;
import static fj.data.Option.*;
import static fj.data.Set.*;
import static fj.pre.Ord.*;
import org.apache.commons.vfs.*;
import org.apache.maven.artifact.transform.*;
import org.apache.maven.artifact.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.logging.*;
import org.apache.maven.project.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;
import java.util.TreeMap;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class MojoHelper
{
    public static Execution create( Map formats,
                                    String formatType,
                                    SnapshotTransformation snapshotTransformation,
                                    MavenProjectWrapper project,
                                    boolean debug,
                                    boolean attachedMode,
                                    F<UnixPackage, UnixPackage> validateMojoSettingsAndApplyFormatSpecificSettingsToPackage,
                                    PackagingMojoParameters mojoParameters,
                                    Log log )
        throws MojoFailureException, MojoExecutionException
    {
        PackagingFormat format = (PackagingFormat) formats.get( formatType );

        if ( format == null )
        {
            throw new MojoFailureException( "Internal error, could not find format for type '" + formatType + "'." );
        }

        // TODO: This is using a private Maven API that might change. Perhaps use some reflection magic here.
        String timestamp = snapshotTransformation.getDeploymentTimestamp();

        FileObject buildDirectory;

        try
        {
            FileSystemManager fileSystemManager = VFS.getManager();
            buildDirectory = fileSystemManager.resolveFile( project.buildDirectory.getAbsolutePath() );
        }
        catch ( FileSystemException e )
        {
            throw new MojoExecutionException( "Error while initializing Commons VFS", e);
        }

        PackageVersion version = PackageVersion.packageVersion( project.version, timestamp,
                                                                project.artifact.isSnapshot(), mojoParameters.revision );

        List<P3<UnixPackage, Package, List<AssemblyOperation>>> packages = nil();

        for ( Package pakke : validatePackages( mojoParameters.packages, attachedMode ) )
        {
            try
            {
                String name = "unix/root-" + formatType + pakke.classifier.map( dashString ).orSome( "" );

                FileObject packageRoot = buildDirectory.resolveFile( name );
                packageRoot.createFolder();

                PackageParameters parameters = calculatePackageParameters( format, project, version, mojoParameters, pakke );

                UnixPackage unixPackage = format.start().
                    mavenCoordinates( parameters.groupId, parameters.artifactId ).
                    version( parameters.version ).
                    id( parameters.id ).
                    name( parameters.name ).
                    description( parameters.description ).
                    contact( parameters.contact ).
                    contactEmail( parameters.contactEmail ).
                    license( parameters.license ).
                    architecture( parameters.architecture ).
                    workingDirectory( packageRoot ).
                    debug( debug ).
                    basedir( project.basedir );

                // -----------------------------------------------------------------------
                // Let the implementation add its metadata
                // -----------------------------------------------------------------------

                unixPackage = validateMojoSettingsAndApplyFormatSpecificSettingsToPackage.f( unixPackage );

                // -----------------------------------------------------------------------
                // DO IT
                // -----------------------------------------------------------------------

                // TODO: here the logic should be different if many packages are to be created.
                // Example: name should be taken from mojoParameters if there is only a single package, if not
                //          it should come from the Pakke object. This should also be validated, at least for
                //          name

                List<AssemblyOperation> assemblyOperations =
                    createAssemblyOperations( project, mojoParameters, pakke, unixPackage, buildDirectory );

                packages = packages.cons( P.p(unixPackage, pakke, assemblyOperations ) );
            }
            catch ( UnknownArtifactException e )
            {
                Map map = new TreeMap<String, Artifact>( e.artifactMap );

                // TODO: Do not log here, throw a CouldNotFindArtifactException with the map as an argument
                log.warn("Could not find artifact:" + e.artifact );
                log.warn("Available artifacts:");
                for ( Object o : map.keySet() )
                {
                    log.warn( o.toString() );
                }

                throw new MojoFailureException( "Unable to find artifact: '" + e.artifact + "'. See log for available artifacts." );
            }
            catch ( MissingSettingException e )
            {
                String msg = "Missing required setting '" + e.getSetting() + "'";
                if ( !pakke.classifier.isNone() )
                {
                    msg += ", for '" + pakke.classifier.some() + "'";
                }
                msg += ", format '" + formatType + "'.";
                throw new MojoFailureException( msg );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error creating package '" + pakke.classifier + "', format '" + formatType + "'.", e );
            }
        }

        return new Execution( packages, project, formatType, attachedMode );
    }

    public static class Execution
    {
        private final List<P3<UnixPackage, Package, List<AssemblyOperation>>> packages;

        private final MavenProjectWrapper project;

        private final String formatType;

        private final boolean attachedMode;

        public Execution( List<P3<UnixPackage, Package, List<AssemblyOperation>>> packages, MavenProjectWrapper project,
                          String formatType, boolean attachedMode )
        {
            this.packages = packages;
            this.project = project;
            this.formatType = formatType;
            this.attachedMode = attachedMode;
        }

        public void execute( MavenProject mavenProject, MavenProjectHelper mavenProjectHelper )
            throws MojoExecutionException, MojoFailureException
        {
            for ( P3<UnixPackage, Package, List<AssemblyOperation>> p : packages )
            {
                UnixPackage unixPackage = p._1();
                Package pakke = p._2();

                try
                {
                    // -----------------------------------------------------------------------
                    // Assemble all the files
                    // -----------------------------------------------------------------------

                    for ( AssemblyOperation assemblyOperation : p._3() )
                    {
                        assemblyOperation.perform( unixPackage );
                    }

                    // -----------------------------------------------------------------------
                    // Package the stuff
                    // -----------------------------------------------------------------------

                    String name = project.artifactId +
                        pakke.classifier.map( dashString ).orSome( "" ) +
                        "-" + unixPackage.getVersion().getMavenVersion() +
                        "." + unixPackage.getPackageFileExtension();

                    File packageFile = new File( project.buildDirectory, name );

                    unixPackage.
                        packageToFile( packageFile );

                    attach( pakke, unixPackage, packageFile, mavenProject, mavenProjectHelper, attachedMode );
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

        private void attach( Package pakke, UnixPackage unixPackage, File packageFile,
                             MavenProject project, MavenProjectHelper mavenProjectHelper, boolean attachedMode )
        {
            if ( attachedMode )
            {
                // In attached mode all the packages are required to have an classifier
                mavenProjectHelper.attachArtifact( project, unixPackage.getPackageFileExtension(), pakke.classifier.some(),
                                                   packageFile );
            }
            else
            {
                if ( pakke.classifier.isNone() )
                {
                    project.getArtifact().setFile( packageFile );
                }
                else
                {
                    mavenProjectHelper.attachArtifact( project, formatType, pakke.classifier.some(), packageFile );
                }
            }
        }
    }

    public static class PackageParameters
    {
        public final String groupId;
        public final String artifactId;
        public final PackageVersion version;
        public final String id;
        public final Option<String> name;
        public final Option<String> description;
        public final Option<String> contact;
        public final Option<String> contactEmail;
        public final String license;
        public final String architecture;

        public PackageParameters( String groupId, String artifactId, PackageVersion version, String id,
                                  Option<String> name, Option<String> description, Option<String> contact,
                                  Option<String> contactEmail, String license, String architecture )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.id = id;
            this.name = name;
            this.description = description;
            this.contact = contact;
            this.contactEmail = contactEmail;
            this.license = license;
            this.architecture = architecture;
        }
    }

    public static PackageParameters calculatePackageParameters( PackagingFormat format, MavenProjectWrapper project,
                                                                PackageVersion version,
                                                                PackagingMojoParameters mojoParameters,
                                                                Package pakke )
    {
        String defaultId = project.groupId + "-" + project.artifactId;

        if ( pakke.classifier.isSome() )
        {
            defaultId += "-" + pakke.classifier.some();
        }

        return new PackageParameters( project.groupId, project.artifactId,
                                      version,
                                      pakke.id.orSome( defaultId.toLowerCase() ),
                                      pakke.name.orElse( mojoParameters.name ).orElse( project.name ),
                                      pakke.description.orElse( project.description ),
                                      mojoParameters.contact,
                                      mojoParameters.contactEmail,
                                      getLicense( format, project ),
                                      mojoParameters.architecture.orSome( format.defaultArchitecture() ) );
    }

    public static List<AssemblyOperation> createAssemblyOperations( MavenProjectWrapper project,
                                                                    PackagingMojoParameters mojoParameters,
                                                                    Package pakke, UnixPackage unixPackage,
                                                                    FileObject basedir )
        throws IOException, MojoFailureException, UnknownArtifactException
    {
        Defaults defaults = mojoParameters.defaults.orSome( new Defaults() );

        List<AssemblyOperation> operations = nil();

        // TODO: Add defaults from the package
        FileAttributes defaultFileAttributes = defaults.getFileAttributes();
        FileAttributes defaultDirectoryAttributes = defaults.getDirectoryAttributes();

        System.out.println( "defaultFileAttributes = " + defaultFileAttributes );
        System.out.println( "defaultDirectoryAttributes = " + defaultDirectoryAttributes );

        unixPackage.beforeAssembly( defaultDirectoryAttributes );

        for ( AssemblyOp assemblyOperation : mojoParameters.assembly.append( pakke.assembly ) )
        {
            assemblyOperation.setArtifactMap( project.artifactConflictIdMap );

            operations = operations.cons( assemblyOperation.createOperation( basedir, defaultFileAttributes,
                                                                             defaultDirectoryAttributes ) );
        }

        return operations;
    }

    public static List<Package> validatePackages( List<Package> packages, boolean attachedMode )
        throws MojoFailureException
    {
        if ( packages.isEmpty() )
        {
            packages = single( new Package() );
        }

        Set<String> names = empty( stringOrd );
        List<Package> outPackages = nil();

        Option<Package> defaultPackage = none();

        for ( Package pakke : packages )
        {
            if ( pakke.classifier.isNone() || pakke.classifier.some().equals( "default" ) )
            {
                if ( defaultPackage.isSome() )
                {
                    throw new MojoFailureException( "There can only be one package without an classifier." );
                }

                pakke.classifier = none();

                defaultPackage = some( pakke );
            }
            else
            {
                if ( names.member( pakke.classifier.some() ) )
                {
                    throw new MojoFailureException( "Duplicate package classifier: '" + pakke.classifier + "'." );
                }

                names = names.insert( pakke.classifier.some() );
                outPackages = outPackages.cons( pakke );
            }
        }

        if ( attachedMode )
        {
            if ( defaultPackage.isSome() )
            {
                throw new MojoFailureException( "When running in attached mode all packages are required to have an classifier." );
            }

            return outPackages;
        }

        if ( defaultPackage.isNone() )
        {
            throw new MojoFailureException( "When running in 'primary artifact mode' exactly one package is required to have 'default' or none classifier." );
        }

        return defaultPackage.toList().append( outPackages );
    }

    protected static String defaultValue( String value, String defaultValue )
    {
        return StringUtils.isNotEmpty( value ) ? value : defaultValue;
    }

    private static String getLicense( PackagingFormat format, MavenProjectWrapper project )
    {
        if ( project.licenses.size() == 0 )
        {
            if ( format.licenseRequired() )
            {
                throw new RuntimeException( "At least one license is required" );
            }

            return null;
        }

        return project.licenses.get( 0 ).getName();
    }

    static F<String, String> dashString = new F<String, String>()
    {
        public String f( String s )
        {
            return "-" + s;
        }
    };
}
