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
import static fj.Function.*;
import static fj.P.*;
import fj.data.List;
import static fj.data.List.join;
import static fj.data.List.*;
import static fj.data.List.single;
import fj.data.*;
import static fj.data.Option.*;
import fj.data.Set;
import static fj.data.Set.*;
import static fj.pre.Ord.*;
import org.apache.commons.logging.*;
import org.apache.commons.vfs.*;
import org.apache.maven.artifact.*;
import org.apache.maven.artifact.transform.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.PackageParameters.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.java.*;
import static org.codehaus.mojo.unix.java.StringF.*;
import org.codehaus.mojo.unix.maven.logging.*;
import org.codehaus.mojo.unix.maven.plugin.*;
import org.codehaus.mojo.unix.maven.plugin.Package;
import static org.codehaus.mojo.unix.util.FileModulator.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;

import java.io.*;
import static java.lang.String.*;
import java.util.*;
import java.util.TreeMap;

/**
 * Utility class encapsulating how to create a package. Used by all packaging Mojos.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class MojoHelper
{
    public static final String ATTACHED_NO_ARTIFACTS_CONFIGURED = "When running in attached mode at least one package has to be configured.";
    public static final String DUPLICATE_CLASSIFIER = "Duplicate package classifier: '%s'.";
    public static final String DUPLICATE_UNCLASSIFIED = "There can only be one package without an classifier.";

    static
    {
        System.setProperty( LogFactory.class.getName(), MavenCommonLoggingLogFactory.class.getName() );
    }

    public static Execution create( Map platforms,
                                    String platformType,
                                    Map formats,
                                    String formatType,
                                    SnapshotTransformation snapshotTransformation,
                                    MavenProjectWrapper project,
                                    boolean debug,
                                    boolean attachedMode,
                                    F<UnixPackage, UnixPackage> validateMojoSettingsAndApplyFormatSpecificSettingsToPackage,
                                    PackagingMojoParameters mojoParameters,
                                    final Log log )
        throws MojoFailureException, MojoExecutionException
    {
        MavenCommonLoggingLogFactory.setMavenLogger( log );

        PackagingFormat format = (PackagingFormat) formats.get( formatType );

        if ( format == null )
        {
            throw new MojoFailureException( "INTERNAL ERROR: could not find format: '" + formatType + "'." );
        }

        UnixPlatform platform = (UnixPlatform) platforms.get( platformType );

        if ( platform == null )
        {
            throw new MojoFailureException( "INTERNAL ERROR: could not find platform: '" + platformType + "'." );
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

                PackageParameters parameters = calculatePackageParameters( project,
                                                                           version,
                                                                           platform,
                                                                           mojoParameters,
                                                                           pakke );

                UnixPackage unixPackage = format.start().
                    parameters( parameters ).
                    setVersion( version ).                      // TODO: This should go away
                    workingDirectory( packageRoot ).
                    debug( debug ).
                    basedir( project.basedir );

                // -----------------------------------------------------------------------
                // Let the implementation add its metadata
                // -----------------------------------------------------------------------

                unixPackage = validateMojoSettingsAndApplyFormatSpecificSettingsToPackage.f( unixPackage );

                // TODO: here the logic should be different if many packages are to be created.
                // Example: name should be taken from mojoParameters if there is only a single package, if not
                //          it should come from the Pakke object. This should also be validated, at least for
                //          name

                List<AssemblyOperation> assemblyOperations = createAssemblyOperations( project,
                                                                                       parameters,
                                                                                       unixPackage,
                                                                                       project.basedir,
                                                                                       buildDirectory,
                                                                                       mojoParameters.assembly,
                                                                                       pakke.assembly );

                // -----------------------------------------------------------------------
                // Dump the execution
                // -----------------------------------------------------------------------

                if ( debug )
                {
                    log.info( "=======================================================================" );
                    log.info( "Package parameters: " + parameters.id );
                    log.info( "Default file attributes: " );
                    log.info( " File      : " + parameters.defaultFileAttributes );
                    log.info( " Directory : " + parameters.defaultDirectoryAttributes );

                    log.info( "Assembly operations: " );
                    for ( AssemblyOperation operation : assemblyOperations )
                    {
                        operation.streamTo( new AbstractLineStreamWriter()
                        {
                            protected void onLine( String line )
                            {
                                log.info( line );
                            }
                        } );
                    }
                }

                packages = packages.cons( p(unixPackage, pakke, assemblyOperations ) );
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

        public void execute( String artifactType, MavenProject mavenProject, MavenProjectHelper mavenProjectHelper, ScriptUtil.Strategy strategy )
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
                        packageToFile( packageFile, strategy );

                    attach( pakke.classifier, artifactType, packageFile, mavenProject, mavenProjectHelper,
                            attachedMode );
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

        private void attach( Option<String> classifier, String artifactType, File packageFile,
                             MavenProject project, MavenProjectHelper mavenProjectHelper, boolean attachedMode )
        {
            if ( attachedMode )
            {
                // In attached mode all the packages are required to have an classifier - this used to be correct - trygve
                // For some reason it is allowed to have attached artifacts without classifier as long as the types differ

                if ( classifier.isSome() )
                {
                    mavenProjectHelper.attachArtifact( project, artifactType, classifier.some(), packageFile );
                }
                else
                {
                    mavenProjectHelper.attachArtifact( project, artifactType, null, packageFile );
                }
            }
            else
            {
                if ( classifier.isNone() )
                {
                    project.getArtifact().setFile( packageFile );
                }
                else
                {
                    mavenProjectHelper.attachArtifact( project, formatType, classifier.some(), packageFile );
                }
            }
        }
    }

    public static PackageParameters calculatePackageParameters( final MavenProjectWrapper project,
                                                                PackageVersion version,
                                                                UnixPlatform platform,
                                                                PackagingMojoParameters mojoParameters,
                                                                final Package pakke )
    {
        String id = pakke.id.orSome( new P1<String>()
        {
            public String _1()
            {
                // This used to be ${groupId}-${artifactId}, but it was too long for pkg so this is a more sane default
                return project.artifactId + pakke.classifier.map( dashString ).orSome( "" );
            }
        } );

        P2<FileAttributes, FileAttributes> defaultFileAttributes =
            calculateDefaultFileAttributes( platform,
                                            mojoParameters.defaults,
                                            pakke.defaults );

        String name = pakke.name.orElse( mojoParameters.name ).orSome( project.name );
        return packageParameters( project.groupId, project.artifactId, version, id, name, pakke.classifier, defaultFileAttributes._1(), defaultFileAttributes._2() ).
            description( pakke.description.orElse( mojoParameters.description ).orElse( project.description ) ).
            contact( mojoParameters.contact ).
            contactEmail( mojoParameters.contactEmail ).
            license( getLicense( project ) ).
            architecture( mojoParameters.architecture );
    }

    public static P2<FileAttributes, FileAttributes> calculateDefaultFileAttributes( UnixPlatform platform,
                                                                                     Defaults mojo,
                                                                                     Defaults pakke )
    {
        return p(calculateFileAttributes( platform.getDefaultFileAttributes(),
                                          mojo.fileAttributes.create(),
                                          pakke.fileAttributes.create() ),
                 calculateFileAttributes( platform.getDefaultDirectoryAttributes(),
                                          mojo.directoryAttributes.create(),
                                          pakke.directoryAttributes.create() ) );
    }

    public static FileAttributes calculateFileAttributes( FileAttributes platform,
                                                          FileAttributes mojo,
                                                          FileAttributes pakke )
    {
        // Calculate default file and directory attributes.
        // Priority order (last one wins): platform -> mojo defaults -> package defaults

        return platform.
            useAsDefaultsFor( mojo ).
            useAsDefaultsFor( pakke );
    }

    public static List<AssemblyOperation> createAssemblyOperations( MavenProjectWrapper project,
                                                                    PackageParameters parameters,
                                                                    UnixPackage unixPackage,
                                                                    File basedir,
                                                                    FileObject buildDirectory,
                                                                    List<AssemblyOp> mojoAssembly,
                                                                    List<AssemblyOp> packageAssembly )
        throws IOException, MojoFailureException, UnknownArtifactException
    {
        unixPackage.beforeAssembly( parameters.defaultDirectoryAttributes );

        // Create the default set of assembly operations
        String unix = new File( basedir, "src/main/unix/files" ).getAbsolutePath();

        String classifierOrDefault = parameters.classifier.orSome( "default" );

        List<AssemblyOp> defaultAssemblyOp = nil();

        // It would be possible to use the AssemblyOperations here but this just make it easier to document
        // as it has a one-to-one relationship with what the user would configure in a POM
        for ( String s : modulatePath( classifierOrDefault, unixPackage.getPackageFileExtension(), unix ) )
        {
            File file = new File( s );

            if ( !file.isDirectory() )
            {
                continue;
            }

            CopyDirectory op = new CopyDirectory();
            op.setFrom( file );
            defaultAssemblyOp = defaultAssemblyOp.cons( op );
        }

        // Create the complete list of assembly operations.
        // Order: defaults -> mojo -> pakke
        List<AssemblyOp> assemblyOps = join( list( defaultAssemblyOp.reverse(), mojoAssembly, packageAssembly ) );

        List<AssemblyOperation> operations = nil();

        for ( AssemblyOp assemblyOp : assemblyOps )
        {
            assemblyOp.setArtifactMap( project.artifactConflictIdMap );

            AssemblyOperation operation = assemblyOp.createOperation( buildDirectory,
                parameters.defaultFileAttributes,
                parameters.defaultDirectoryAttributes );

            operations = operations.cons( operation );
        }

        return operations.reverse();
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
            if ( pakke.classifier.exists( curry( StringF.equals, "default" ) ) )
            {
                pakke.classifier = none();
            }

            if ( pakke.classifier.isNone() )
            {
                if ( defaultPackage.isSome() )
                {
                    throw new MojoFailureException( DUPLICATE_UNCLASSIFIED );
                }

                defaultPackage = some( pakke );
            }
            else
            {
                if ( names.member( pakke.classifier.some() ) )
                {
                    throw new MojoFailureException( format( DUPLICATE_CLASSIFIER, pakke.classifier ) );
                }

                names = names.insert( pakke.classifier.some() );
                outPackages = outPackages.cons( pakke );
            }
        }

        if ( attachedMode )
        {
            outPackages = defaultPackage.toList().append( outPackages );

            if ( outPackages.isEmpty() )
            {
                throw new MojoFailureException( ATTACHED_NO_ARTIFACTS_CONFIGURED );
            }

            return outPackages;
        }

        if ( defaultPackage.isNone() )
        {
            throw new MojoFailureException( "When running in 'primary artifact mode' either one package has to have 'default' as classifier or there has to be one without any classifier." );
        }

        return defaultPackage.toList().append( outPackages );
    }

    private static Option<String> getLicense( MavenProjectWrapper project )
    {
        if ( project.licenses.size() == 0 )
        {
            return none();
        }

        return some( project.licenses.get( 0 ).getName() );
    }

    static F<String, String> dashString = curry( concat, "-" );
}
