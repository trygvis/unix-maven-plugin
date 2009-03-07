package org.codehaus.mojo.unix.maven.util;

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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.core.AssemblyOperation;
import org.codehaus.mojo.unix.maven.AssemblyOp;
import org.codehaus.mojo.unix.maven.Defaults;
import org.codehaus.mojo.unix.maven.Package;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility to handle the creation of a single package.
 *
 * Handles selecting the correct value from either the POM, the configured plugin properties and per package
 * configuration.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageCreationUtil
{
    private final Package pakke;

    private final String classifier;

    private final MavenProject project;

    private final UnixPackage unixPackage;

    private final Map<String, Artifact> artifactMap;

    private final Defaults defaults;

    private final List<AssemblyOp> assemblyOperations = new LinkedList<AssemblyOp>();

    public PackageCreationUtil( Package pakke, String classifier, MavenProject project, UnixPackage unixPackage,
                                Map<String, Artifact> artifactMap, Defaults defaults )
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
    // specific value
    // -----------------------------------------------------------------------

    public PackageCreationUtil name( String packageName, String artifactIdBasedName )
        throws MissingSettingException
    {
        unixPackage.name( select( packageName, artifactIdBasedName ) );
        return this;
    }

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

    public PackageCreationUtil appendAssemblyOperations( AssemblyOp[] assemblyOperations )
    {
        if ( assemblyOperations == null )
        {
            return this;
        }
        this.assemblyOperations.addAll( Arrays.asList( assemblyOperations ) );

        return this;
    }

    public File createPackage( FileObject basedir )
        throws Exception
    {
        // -----------------------------------------------------------------------
        // Assemble all the files
        // -----------------------------------------------------------------------

        assemble( basedir, assemblyOperations );

        // -----------------------------------------------------------------------
        // Package the stuff
        // -----------------------------------------------------------------------

        String name = project.getArtifactId() +
            ( classifier == null ? "" : "-" + pakke.getId() ) +
            "-" + unixPackage.getVersion().getMavenVersion() +
            "." + unixPackage.getPackageFileExtension();

        File packageFile = new File( project.getBuild().getDirectory(), name );

        unixPackage.
            packageToFile( packageFile );

        return packageFile;
    }

    private void assemble( FileObject basedir, List<AssemblyOp> assembly )
        throws MojoFailureException, MojoExecutionException
    {
        try
        {
            for ( AssemblyOp assemblyOperation : assembly )
            {
                assemblyOperation.setArtifactMap( artifactMap );
                AssemblyOperation operation = assemblyOperation.createOperation( basedir, defaults );

                operation.perform( unixPackage );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while assembling package.", e );
        }
    }

    private String select( String a, String b )
    {
        return StringUtils.isNotEmpty(a) ? a : b;
    }

    private String select( String setting, String a, String b )
        throws MissingSettingException
    {
        if ( StringUtils.isNotEmpty( a ) )
        {
            return a;
        }

        if ( StringUtils.isNotEmpty( b ) )
        {
            return b;
        }

        throw new MissingSettingException( setting );
    }
}
