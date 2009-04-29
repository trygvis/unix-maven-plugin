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
import static org.codehaus.mojo.unix.maven.MojoHelper.*;
import org.codehaus.mojo.unix.maven.pkg.*;
import org.codehaus.mojo.unix.util.*;

import java.util.*;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: MojoHelper.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class MojoHelperTest
    extends TestCase
{
    public final static Package defaultPackage = new Package();

    public final static Package packageA = new Package();

    public final static Package packageB = new Package();

    static
    {
        packageA.classifier = some( "a" );
        packageB.classifier = some( "b" );
    }

    public void testPackageValidationEmptyPackagesPrimaryArtifact()
        throws Exception
    {
        List<Package> configuredPackages = nil();
        boolean attachedMode = false;

        List<Package> packages = MojoHelper.validatePackages( configuredPackages, attachedMode );

        assertEquals( 1, packages.length() );
        Package pakke = packages.index( 0 );
        assertTrue( pakke.classifier.isNone() );
    }

    public void testPackageValidationDefaultPackagePrimaryArtifact()
        throws Exception
    {
        List<Package> configuredPackages = list( defaultPackage );
        boolean attachedMode = false;

        List<Package> packages = MojoHelper.validatePackages( configuredPackages, attachedMode );

        assertEquals( 1, packages.length() );
        Package pakke = packages.index( 0 );
        assertTrue( pakke.classifier.isNone() );
    }

    public void testPackageValidationPackageAPrimaryArtifact()
        throws Exception
    {
        List<Package> configuredPackages = list( packageA );
        boolean attachedMode = false;

        List<Package> packages = MojoHelper.validatePackages( configuredPackages, attachedMode );

        assertEquals( 2, packages.length() );
        Package pakke = packages.index( 0 );
        assertTrue( pakke.classifier.isNone() );
        pakke = packages.index( 1 );
        assertTrue( pakke.classifier.isSome() );
    }

    public void testPackageValidationEmptyPackagesAsAttached()
    {
        List<Package> configuredPackages = nil();
        boolean attachedMode = true;
        try
        {
            MojoHelper.validatePackages( configuredPackages, attachedMode );
            fail( "Expected Exception" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( "When running in attached mode all packages are required to have an classifier.", e.getMessage() );
        }
    }

    public void testPackageValidationDefaultPackageAsAttached()
    {
        List<Package> configuredPackages = single( defaultPackage );
        boolean attachedMode = true;
        try
        {
            MojoHelper.validatePackages( configuredPackages, attachedMode );
            fail( "Expected Exception" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( "When running in attached mode all packages are required to have an classifier.", e.getMessage() );
        }
    }

    public void testPackageValidationPackagesAAsAttached()
        throws MojoFailureException
    {
        List<Package> configuredPackages = single( packageA );
        boolean attachedMode = true;

        List<Package> packages = MojoHelper.validatePackages( configuredPackages, attachedMode );
        assertEquals( 1, packages.length() );
        Package pakke = packages.index( 0 );
        assertTrue( pakke.classifier.isSome() );
    }

    public void testNameOverriding()
    {
        String projectName = "Project Name";
        String mojoName = "Mojo Name";
        String packageName = "Package Name";

        assertName( some( packageName ), projectName, mojoName, packageName );
        assertName( some( packageName ), null, mojoName, packageName );
        assertName( some( packageName ), projectName, null, packageName );
        assertName( some( packageName ), null, null, packageName );

        assertName( some( mojoName ), projectName, mojoName, null );
        assertName( some( mojoName ), null, mojoName, null );

        assertName( some( projectName ), projectName, null, null );
    }

    private void assertName( Option<String> expectedName, String projectName, String mojoName, String packageName )
    {
        Set<Artifact> artifactSet = Collections.emptySet();
        java.util.List<License> licenses = Collections.emptyList();
        Map<String, Artifact> artifactMap = Collections.emptyMap();

        PackageVersion version = PackageVersion.create( "1.0", "123456.123456", false, null, null );

        MavenProjectWrapper mavenProject = new MavenProjectWrapper( "groupId", "artifactId", "1.0", null, projectName,
                                                                    null, null, null, artifactSet, licenses,
                                                                    artifactMap );

        PackagingMojoParameters mojoParameters = new PackagingMojoParameters( mojoName, mavenProject.version, null,
                                                                              "Description", "A B", "a@b.com", "all",
                                                                              new Defaults(), new AssemblyOp[0],
                                                                              new Package[0] );

        Package pakke = new Package();
        pakke.id = fromNull( packageName );
        PackageParameters parameters = calculatePackageParameters( new PkgPackagingFormat(), mavenProject, version,
                                                                   mojoParameters, pakke );

        UnixUtil.optionEquals( expectedName, parameters.name );
    }
}
