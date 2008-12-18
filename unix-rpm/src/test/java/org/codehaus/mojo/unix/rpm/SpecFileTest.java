package org.codehaus.mojo.unix.rpm;

import junit.framework.TestCase;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFileTest
    extends TestCase
{
    public final String header = "Name: groupid-artifactid\n" +
        "Version: 1.0\n" +
        "Release: 1\n" +
        "Summary: My summary\n" +
        "License: License\n" +
        "Group: My Group\n" +
        "BuildRoot: /build-root\n";

    public void testFilesGeneration()
        throws MissingSettingException
    {
        SpecFile specFile = testSpecFile();

        specFile.addDirectory( "/usr/bin", "myuser", "mygroup", null );

        assertEquals( header +
            "\n" +
            "%description\n" +
            "\n" +
            "%files\n" +
            "%dir %attr(-,myuser,mygroup) /usr/bin", toString( specFile ) );
    }

    public void testDescription()
        throws MissingSettingException
    {
        SpecFile specFile = testSpecFile();

        specFile.description = "Yo yo";

        assertEquals( header +
            "\n" +
            "%description\n" +
            "Yo yo\n" +
            "\n" +
            "%files", toString( specFile ) );
    }

    public void testScriptGeneration()
        throws MissingSettingException
    {
        SpecFile specFile = testSpecFile();

        specFile.includePost = new File( "pom.xml" );

        assertEquals( header +
            "\n" +
            "%description\n" +
            "\n" +
            "%files\n" +
            "\n" +
            "%post\n" +
            "%include " + specFile.includePost.getAbsolutePath(), toString( specFile ) );
    }

    private String toString( SpecFile specFile )
        throws MissingSettingException
    {
        CharArrayWriter actual = new CharArrayWriter();
        PrintWriter writer = new PrintWriter( actual );
        specFile.writeTo( writer );
        return actual.toString().trim();
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
        specFile.buildRoot = new File( "/build-root" );
        return specFile;
    }
}
