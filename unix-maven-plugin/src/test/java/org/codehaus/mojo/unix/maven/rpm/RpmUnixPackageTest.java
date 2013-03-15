package org.codehaus.mojo.unix.maven.rpm;

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

import fj.data.*;
import org.apache.maven.plugin.logging.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.PackageParameters.*;
import static org.codehaus.mojo.unix.PackageVersion.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;

import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.maven.*;
import org.codehaus.mojo.unix.rpm.*;

import static org.codehaus.mojo.unix.io.fs.FsUtil.resolve;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import static org.codehaus.mojo.unix.util.ScriptUtil.Strategy.SINGLE;
import org.codehaus.plexus.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RpmUnixPackageTest
    extends PlexusTestCase
{
    private final LocalDateTime now = new LocalDateTime();

    public void testBasic()
        throws Exception
    {
        LocalFs pomXml = new LocalFs( getTestFile( "pom.xml" ) );
        Fs archive = resolve( getTestFile( "../unix-core/src/test/resources/operation/extract.jar" ) );
        Fs<?> fooLicense = archive.resolve( relativePath( "foo-license.txt" ) );
        Fs<?> barLicense = archive.resolve( relativePath( "mydir/bar-license.txt" ) );

        RpmPackagingFormat packagingFormat = new RpmPackagingFormat();

        LocalFs root = new LocalFs( getTestFile( "target/rpm-test" ) );
        File packageFile = root.resolve( "file.rpm" ).file;

        PackageVersion version = packageVersion( "1.0-1", "123", false, Option.<String>none() );
        PackageParameters parameters = packageParameters( "mygroup", "myartifact", version, "id", "default-name",
                                                          Option.<String>none(), EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "noarch" ).
            name( "Yo!" ).
            license( "BSD" );

        RpmUnixPackage unixPackage = packagingFormat.start( new SystemStreamLog() ).
            parameters( parameters ).
            rpmParameters( "Fun", Option.<String>none() ).
            workingDirectory( root.resolve( "working-directory" ) );

        unixPackage.beforeAssembly( EMPTY, now );

        unixPackage.addFile( pomXml, regularFile( relativePath( "/pom.xml" ), now, 0, EMPTY ) );
        unixPackage.addFile( fooLicense, regularFile( relativePath( "/foo-license.txt" ), now, 0, EMPTY ) );
        unixPackage.addFile( barLicense, regularFile( relativePath( "/bar-license.txt" ), now, 0, EMPTY ) );

        RpmUnixPackage.RpmPreparedPackage preparedPackage = unixPackage.
            debug( true ).
            prepare( SINGLE );

        if ( !new Rpmbuild().available() )
        {
            System.err.println( "Skipping test: " + super.getName() );
            return;
        }

        preparedPackage.
            packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }

    public void testFiltering()
        throws Exception
    {
        RpmPackagingFormat packagingFormat = new RpmPackagingFormat();

        UnixPackageTestUtil<RpmUnixPackage, RpmUnixPackage.RpmPreparedPackage> unixPackageTestUtil =
            new UnixPackageTestUtil<RpmUnixPackage, RpmUnixPackage.RpmPreparedPackage>( "rpm", packagingFormat  )
            {
                protected RpmUnixPackage extraStuff( RpmUnixPackage rpmUnixPackage )
                {
                    return rpmUnixPackage.rpmParameters( "my-group", Option.<String>none() );
                }
            };
        unixPackageTestUtil.testFiltering();
    }
}
