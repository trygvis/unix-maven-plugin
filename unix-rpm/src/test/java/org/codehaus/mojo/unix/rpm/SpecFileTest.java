package org.codehaus.mojo.unix.rpm;

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
import fj.data.*;
import static fj.data.Option.*;
import junit.framework.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.UnixFileMode.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFileTest
    extends TestCase
{
    final LineFile header;

    final LocalDateTime lastModified = new LocalDateTime( 2009, 2, 24, 9, 42 );

    public SpecFileTest()
        throws IOException
    {
        header = new LineFile();
        header.
            add( "Name: groupid-artifactid" ).
            add( "Version: 1.0" ).
            add( "Release: 1" ).
            add( "Summary: My summary" ).
            add( "License: License" ).
            add( "Group: My Group" ).
            add( "BuildRoot: " + new File( "build-root" ).getAbsolutePath() );
    }

    public void testFilesGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        Option<String> mygroup = some( "mygroup" );
        Option<String> myuser = Option.some( "myuser" );

        FileAttributes usrbinAttributes = new FileAttributes( myuser, mygroup, some( _0755 ) );
        FileAttributes binAttributes = new FileAttributes( myuser, mygroup, UnixFileMode.none );

        UnixFsObject.Directory usrbin = directory( relativePath( "/usr/bin" ), lastModified, usrbinAttributes );
        UnixFsObject.Directory bin = directory( relativePath( "/bin" ), lastModified, binAttributes );

        FileAttributes fileAttributes = new FileAttributes( myuser, mygroup, some( _0644 ) ).addTag( "unused" );
        RelativePath extract2Jar = relativePath( "/extract2.jar" );
        FileAttributes extract2JarAttributes = fileAttributes.user( "extract" );

        specFile.addDirectory( usrbin );
        specFile.addDirectory( bin );
        specFile.addFile( regularFile( relativePath( "/extract.jar" ), lastModified, 10, some( fileAttributes ) ) );
        specFile.addFile( regularFile( extract2Jar, lastModified, 10, some( fileAttributes ) ) );
        specFile.addFile( regularFile( relativePath( "/a" ), lastModified, 10, some( fileAttributes.addTag( "doc" ) ) ) );
        specFile.addFile( regularFile( relativePath( "/b" ), lastModified, 10, some( fileAttributes.addTag( "config" ) ) ) );
        specFile.addFile( regularFile( relativePath( "/c" ), lastModified, 10, some( fileAttributes.addTag( "rpm:missingok" ) ) ) );
        specFile.addFile( regularFile( relativePath( "/d" ), lastModified, 10, some( fileAttributes.addTag( "rpm:noreplace" ) ) ) );
        specFile.addFile( regularFile( relativePath( "/e" ), lastModified, 10, some( fileAttributes.addTag( "rpm:ghost" ) ) ) );
        specFile.apply( filter( extract2Jar, extract2JarAttributes ) );

        assertEquals( header.
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add( "%doc %attr(0644,myuser,mygroup) /a" ).
            add( "%config %attr(0644,myuser,mygroup) /b" ).
            add( "%dir %attr(-,myuser,mygroup) /bin" ).
            add( "%config(missingok) %attr(0644,myuser,mygroup) /c" ).
            add( "%config(noreplace) %attr(0644,myuser,mygroup) /d" ).
            add( "%ghost %attr(0644,myuser,mygroup) /e" ).
            add( "%attr(0644,myuser,mygroup) /extract.jar" ).
            add( "%attr(0644,extract,mygroup) /extract2.jar" ).
            add( "%dir %attr(-,root,root) /usr" ).
            add( "%dir %attr(0755,myuser,mygroup) /usr/bin" ).
            toString(), toString( specFile ) );
    }

    public void testDescription()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        specFile.description = "Yo yo";

        assertEquals( header.
            add().
            add( "%description" ).
            add( "Yo yo" ).
            add().
            add( "%files" ).
            toString(), toString( specFile ) );
    }

    public void testScriptGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        specFile.includePost = some( new File( "pom.xml" ) );

        assertEquals( header.
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add().
            add( "%post" ).
            add( "%include " + specFile.includePost.some().getAbsolutePath() ).toString(), toString( specFile ) );
    }

    private String toString( SpecFile specFile )
        throws Exception
    {
        LineFile spec = new LineFile();
        specFile.streamTo( spec );
        return spec.toString();
    }

    private SpecFile testSpecFile()
    {
        FileAttributes fileAttributes = EMPTY.user( "root" ).group( "root" );

        SpecFile specFile = new SpecFile();
        specFile.name = "groupid-artifactid";
        specFile.version = "1.0";
        specFile.release = "1";
        specFile.summary = "My summary";
        specFile.license = "License";
        specFile.group = "My Group";
        specFile.buildRoot = new File( "build-root" );
        specFile.beforeAssembly( UnixFsObject.directory( BASE, new LocalDateTime(), fileAttributes ) );
        return specFile;
    }

    private F2<UnixFsObject, FileAttributes, FileAttributes> filter( final RelativePath path, final FileAttributes newAttributes )
    {
        return new F2<UnixFsObject, FileAttributes, FileAttributes>()
        {
            public FileAttributes f( UnixFsObject fsObject, FileAttributes attributes )
            {
                return !fsObject.path.isBelowOrSame( path ) ? attributes : attributes.useAsDefaultsFor( newAttributes );
            }
        };
    }
}
