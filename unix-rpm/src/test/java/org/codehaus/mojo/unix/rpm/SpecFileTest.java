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

import fj.F2;
import fj.data.Option;
import static fj.data.Option.some;
import junit.framework.TestCase;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.UnixFileMode;
import static org.codehaus.mojo.unix.UnixFileMode._0644;
import static org.codehaus.mojo.unix.UnixFileMode._0755;
import org.codehaus.mojo.unix.UnixFsObject;
import static org.codehaus.mojo.unix.UnixFsObject.directory;
import static org.codehaus.mojo.unix.UnixFsObject.regularFile;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;

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

        UnixFsObject.Directory usrbin = directory( fromString( "/usr/bin" ), lastModified, usrbinAttributes );
        UnixFsObject.Directory bin = directory( fromString( "/bin" ), lastModified, binAttributes );

        FileAttributes fileAttributes = new FileAttributes( myuser, mygroup, some( _0644 ) );
        RelativePath extract2Jar = fromString( "/extract2.jar" );
        FileAttributes extract2JarAttributes = fileAttributes.user( "extract" );

        specFile.addDirectory( usrbin );
        specFile.addDirectory( bin );
        specFile.addFile( regularFile( fromString( "/extract.jar" ), lastModified, 10, some( fileAttributes ) ) );
        specFile.addFile( regularFile( extract2Jar, lastModified, 10, some( fileAttributes ) ) );
        specFile.apply( filter( extract2Jar, extract2JarAttributes ) );

        assertEquals( header.
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add( "%dir %attr(-,myuser,mygroup) /bin" ).
            add( "%attr(0644,myuser,mygroup) /extract.jar" ).
            add( "%attr(0644,extract,mygroup) /extract2.jar" ).
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
            add( "%files" ).toString(), toString( specFile ) );
    }

    public void testScriptGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        specFile.includePost = new File( "pom.xml" );

        assertEquals( header.
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add().
            add( "%post" ).
            add( "%include " + specFile.includePost.getAbsolutePath() ).toString(), toString( specFile ) );
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
        SpecFile specFile = new SpecFile();
        specFile.groupId = "groupId";
        specFile.artifactId = "artifactId";
        specFile.version = PackageVersion.create( "1.0", "now", false, null, null );
        specFile.summary = "My summary";
        specFile.license = "License";
        specFile.group = "My Group";
        specFile.buildRoot = new File( "build-root" );
        return specFile;
    }

    private F2<UnixFsObject, FileAttributes, FileAttributes> filter( final RelativePath s, final FileAttributes newAttributes )
    {
        return new F2<UnixFsObject, FileAttributes, FileAttributes>()
        {
            public FileAttributes f( UnixFsObject fsObject, FileAttributes attributes )
            {
                return !fsObject.path.startsWith( s ) ? attributes : attributes.useAsDefaultsFor( newAttributes );
            }
        };
    }
}
