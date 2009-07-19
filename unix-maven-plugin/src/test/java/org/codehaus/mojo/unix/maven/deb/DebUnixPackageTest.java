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
import org.apache.commons.vfs.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.PackageParameters.*;
import static org.codehaus.mojo.unix.PackageVersion.*;
import org.codehaus.mojo.unix.deb.*;
import org.codehaus.mojo.unix.maven.plugin.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.vfs.*;
import org.codehaus.plexus.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DebUnixPackageTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        if ( !Dpkg.available() )
        {
            return;
        }

        DebPackagingFormat packagingFormat = (DebPackagingFormat) lookup( PackagingFormat.ROLE, "deb" );

        FileObject dpkgTest = VFS.getManager().resolveFile( getTestPath( "target/deb-test" ) );
        FileObject packageRoot = dpkgTest.resolveFile( "root" );
        File packageFile = VfsUtil.asFile( dpkgTest.resolveFile( "file.deb" ) );

        PackageVersion version = packageVersion( "1.0", "123", false, some( "1" ) );
        PackageParameters parameters = packageParameters( "mygroup", "myartifact", version, "id", "default-name",
                                                          Option.<java.lang.String>none(), EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "all" );

        List<String> nil = List.nil();
        packagingFormat.start().
            parameters( parameters ).
            debParameters( Option.<String>none(), some( "devel" ), false, nil, nil, nil, nil, nil, nil ).
            debug( true ).
            workingDirectory( packageRoot ).
            packageToFile( packageFile, ScriptUtil.Strategy.SINGLE );

        assertTrue( packageFile.canRead() );
    }
}
