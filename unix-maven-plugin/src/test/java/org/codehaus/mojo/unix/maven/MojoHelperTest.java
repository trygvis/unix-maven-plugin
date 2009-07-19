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

import fj.data.List;
import static fj.data.List.*;
import fj.data.*;
import static fj.data.Option.*;
import junit.framework.*;
import org.apache.maven.artifact.*;
import org.apache.maven.model.*;
import org.apache.maven.plugin.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import static org.codehaus.mojo.unix.maven.MojoHelper.*;
import org.codehaus.mojo.unix.maven.plugin.*;
import org.codehaus.mojo.unix.maven.plugin.Package;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.UnixUtil.*;

import java.util.*;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: MojoHelper.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class MojoHelperTest
    extends TestCase
{
    public final static Package noClassifierPackage = new Package();

    public final static Package defaultPackage = new Package();

    public final static Package packageA = new Package();

    public final static Package packageB = new Package();

    static
    {
        defaultPackage.classifier = some( "default" );
        packageA.classifier = some( "a" );
        packageB.classifier = some( "b" );
    }

    // -----------------------------------------------------------------------
    // Primary
    // -----------------------------------------------------------------------

    public void testPackageValidationEmptyPackagesPrimaryArtifact()
        throws Exception
    {
        assertPackages( list( noClassifierPackage ), List.<Package>nil(), false );
    }

    public void testPackageValidationNoClassifierPackagePrimaryArtifact()
        throws Exception
    {
        assertPackages( list( noClassifierPackage ), list( noClassifierPackage ), false );
    }

    public void testPackageValidationDefaultPackagePrimaryArtifact()
        throws Exception
    {
        assertPackages( list( noClassifierPackage ), list( defaultPackage ), false );
    }

    public void testPackageValidationNoClassifierPackageAPackagePrimaryArtifact()
        throws Exception
    {
        assertPackages( list(  noClassifierPackage, packageA  ), list( noClassifierPackage, packageA ), false );
    }

    public void testPackageValidationDefaultPackageAPackagePrimaryArtifact()
        throws Exception
    {
        assertPackages( list(  noClassifierPackage, packageA  ), list( defaultPackage, packageA ), false );
    }

    public void testPackageValidationPackageAPrimaryArtifact()
        throws Exception
    {
        try
        {
            MojoHelper.validatePackages( single( packageA ), false );
            fail( "Expected Exception" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( "When running in 'primary artifact mode' either one package has to have 'default' as classifier or there has to be one without any classifier.",
                          e.getMessage() );
        }
    }

    public void testPackageValidationTwoUnnamedPrimaryArtifact()
        throws Exception
    {
        try
        {
            MojoHelper.validatePackages( list( noClassifierPackage, noClassifierPackage ), false );
            fail( "Expected Exception" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( MojoHelper.DUPLICATE_UNCLASSIFIED, e.getMessage() );
        }
    }

    public void testPackageValidationTwoDefaultPrimaryArtifact()
        throws Exception
    {
        try
        {
            MojoHelper.validatePackages( list( defaultPackage, defaultPackage ), false );
            fail( "Expected Exception" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( MojoHelper.DUPLICATE_UNCLASSIFIED, e.getMessage() );
        }
    }

    // -----------------------------------------------------------------------
    // Attached
    // -----------------------------------------------------------------------

    public void testPackageValidationEmptyPackagesAsAttached()
        throws MojoFailureException
    {
        assertPackages( list( noClassifierPackage ), List.<Package>nil(), true );
//        // Hm, is running with an empty configuration really a bug? Should result in no packages to be generated
//        // which is ok as long as the build is attached and another (non-unix) artifact will be generated as the
//        // primary artifact - trygve
//
//        try
//        {
//            MojoHelper.validatePackages( List.<Package>nil(), true );
//            fail( "Expected Exception" );
//        }
//        catch ( MojoFailureException e )
//        {
//            assertEquals( MojoHelper.ATTACHED_NO_ARTIFACTS_CONFIGURED,
//                          e.getMessage() );
//        }
    }

    public void testPackageValidationNoClassifierPackageAsAttached()
        throws MojoFailureException
    {
        assertPackages( single( noClassifierPackage ), single( noClassifierPackage ), true );
    }

    public void testPackageValidationDefaultPackageAsAttached()
        throws MojoFailureException
    {
        assertPackages( single( noClassifierPackage ), single( defaultPackage ), true );
    }

    public void testPackageValidationPackagesAAsAttached()
        throws MojoFailureException
    {
        assertPackages( single( packageA ), single( packageA ), true );
    }

    public void testNameOverriding()
    {
        String projectName = "Project Name";
        String mojoName = "Mojo Name";
        String packageName = "Package Name";

        assertName( some( packageName ), projectName, mojoName, packageName );
        assertName( some( mojoName ), projectName, mojoName, null );
        assertName( some( packageName ), projectName, null, packageName );
        assertName( some( projectName ), projectName, null, null );
    }

    private void assertName( Option<String> expectedName, String projectName, String mojoName, String packageName )
    {
        Set<Artifact> artifactSet = Collections.emptySet();
        java.util.List<License> licenses = Collections.emptyList();
        Map<String, Artifact> artifactMap = Collections.emptyMap();

        PackageVersion version = PackageVersion.packageVersion( "1.0", "123456.123456", false, Option.<String>none() );

        MavenProjectWrapper mavenProject = new MavenProjectWrapper( "groupId", "artifactId", "1.0", null, projectName,
                                                                    null, null, null, artifactSet, licenses,
                                                                    artifactMap );

        PackagingMojoParameters mojoParameters = new PackagingMojoParameters( mojoName, null, "Description", "A B",
                                                                              "a@b.com", "all", new Defaults(),
                                                                              new AssemblyOp[0], new Package[0] );

        Package pakke = new Package();
        pakke.id = fromNull( packageName );
        PackageParameters parameters = calculatePackageParameters( mavenProject, version,
                                                                   new SolarisUnixPlatform(),
                                                                   mojoParameters, pakke );

        UnixUtil.optionEquals( expectedName, parameters.name );
    }

    private void assertPackages( List<Package> expectedPackages, List<Package> configuredPackages,
                                 boolean attachedMode )
        throws MojoFailureException
    {
        List<Package> packages = MojoHelper.validatePackages( configuredPackages, attachedMode );

        assertEquals( expectedPackages.length(), packages.length() );
        for ( Package expectedPackage : expectedPackages )
        {
            Package actualPackage = packages.index( 0 );
            optionEquals( expectedPackage.id, actualPackage.id );
        }
    }
}
