package org.codehaus.mojo.unix.maven.dpkg;

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
import org.apache.commons.vfs.VFS;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.dpkg.Dpkg;
import org.codehaus.mojo.unix.maven.PackagingFormat;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackageTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        if ( !Dpkg.available() )
        {
            return;
        }

        DpkgPackagingFormat packagingFormat = (DpkgPackagingFormat) lookup( PackagingFormat.ROLE, "dpkg" );

        FileObject dpkgTest = VFS.getManager().resolveFile( getTestPath("target/dpkg-test") );
        FileObject packageRoot = dpkgTest.resolveFile( "root" );
        File packageFile = VfsUtil.asFile( dpkgTest.resolveFile( "file.deb" ) );

        DpkgUnixPackage.cast( packagingFormat.start() ).
            section( "devel" ).
            debug( true ).
            mavenCoordinates( "mygroup", "myartifact", null ).
            version( PackageVersion.create( "1.0", "123", false, null, 1 ) ).
            contact( "Kurt Cobain" ).
            architecture( "all" ).
            workingDirectory( packageRoot ).
            packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }
}
