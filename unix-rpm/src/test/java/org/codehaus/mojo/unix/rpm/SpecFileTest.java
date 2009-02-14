package org.codehaus.mojo.unix.rpm;

import junit.framework.TestCase;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFileTest
    extends TestCase
{
    public final LineFile header;

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

        specFile.addDirectory( RelativePath.fromString( "/usr/bin" ), new FileAttributes( "myuser", "mygroup", UnixFileMode._0755 ) );
        specFile.addDirectory( RelativePath.fromString( "/bin" ), new FileAttributes( "myuser", "mygroup", null ) );
        specFile.addFile( RelativePath.fromString( "/extract.jar" ), new FileAttributes( "myuser", "mygroup", UnixFileMode._0644 ) );

        assertEquals( header.
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add( "%dir %attr(0755,myuser,mygroup) /usr/bin" ).
            add( "%dir %attr(-,myuser,mygroup) /bin" ).
            add( "%attr(0644,myuser,mygroup) /extract.jar" ).
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
        CharArrayWriter actual = new CharArrayWriter();
        PrintWriter writer = new PrintWriter( actual );
        specFile.writeTo( writer );
        return actual.toString();
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
}
