package org.codehaus.mojo.unix.maven.deb;

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
import static fj.data.Option.*;
import org.apache.maven.plugin.logging.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.PackageParameters.*;
import static org.codehaus.mojo.unix.PackageVersion.*;
import org.codehaus.mojo.unix.deb.*;
import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.maven.*;
import static org.codehaus.mojo.unix.util.ScriptUtil.Strategy.*;
import org.codehaus.plexus.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DebUnixPackageTest
    extends PlexusTestCase
{
    private final LocalDateTime now = new LocalDateTime();

    private final PackageVersion version = packageVersion( "1.0", "123", false, some( "1" ) );

    final PackageParameters parameters =
        packageParameters( "mygroup", "myartifact", version, "id", "default-name", Option.<java.lang.String>none(),
                           EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "all" );

    public void testBasic()
        throws Exception
    {
        DebPackagingFormat packagingFormat = new DebPackagingFormat();

        LocalFs root = new LocalFs( getTestFile( "target/deb-test" ) );
        File packageFile = root.resolve( "file.deb" ).file;

        List<String> nil = List.nil();
        UnixPackage pkg = packagingFormat.start( new SystemStreamLog() ).
            parameters( parameters ).
            debParameters( Option.<String>none(), some( "devel" ), false, Option.<String>none(), nil, nil, nil, nil,
                           nil, nil ).
            debug( true ).
            workingDirectory( root.resolve( "working-directory" ) );
        pkg.beforeAssembly( EMPTY, now );
        UnixPackage.PreparedPackage preparedPackage = pkg.prepare( SINGLE );

        if ( !new DpkgDeb().available() )
        {
            System.err.println( "Skipping test: " + super.getName() );
            return;
        }

        preparedPackage.packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }

    public void testFiltering()
        throws Exception
    {
        DebPackagingFormat packagingFormat = new DebPackagingFormat();

        new UnixPackageTestUtil<DebUnixPackage, DebUnixPackage.DebPreparedPackage>( "deb", packagingFormat ).testFiltering();
    }
}
