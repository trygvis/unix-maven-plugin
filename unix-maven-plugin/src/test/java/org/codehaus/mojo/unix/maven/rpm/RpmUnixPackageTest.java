package org.codehaus.mojo.unix.maven.rpm;

import fj.data.Option;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.PackageVersion;
import static org.codehaus.mojo.unix.UnixFsObject.regularFile;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.PackagingFormat;
import org.codehaus.mojo.unix.rpm.Rpmbuild;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.plexus.PlexusTestCase;
import org.joda.time.LocalDateTime;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUnixPackageTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        if ( !Rpmbuild.available() )
        {
            return;
        }

        String archivePath = getTestPath( "src/test/resources/operation/extract.jar" );

        FileSystemManager fsManager = VFS.getManager();
        FileObject pomXml = fsManager.resolveFile( getTestPath( "pom.xml" ) );
        FileObject archiveObject = fsManager.resolveFile( archivePath );
        FileObject archive = fsManager.createFileSystem( archiveObject );
        FileObject fooLicense = archive.getChild( "foo-license.txt" );
        FileObject barLicense = archive.getChild( "mydir" ).getChild( "bar-license.txt" );

        RpmPackagingFormat packagingFormat = (RpmPackagingFormat) lookup( PackagingFormat.ROLE, "rpm" );

        FileObject rpmTest = VFS.getManager().resolveFile( getTestPath("target/rpm-test") );
        FileObject packageRoot = rpmTest.resolveFile( "root" );
        File packageFile = getTestFile( "target/rpm-test/file.rpm" );

        UnixPackage unixPackage = RpmPackagingFormat.cast( packagingFormat.start().
            mavenCoordinates( "mygroup", "myartifact", null ).
            version( PackageVersion.create( "1.0-1", "123", false, null, 0 ) ).
            contact( "Kurt Cobain" ).
            architecture( "all" ).
            shortDescription( "Yo!" ).
            license( "BSD" ).
            workingDirectory( packageRoot ) ).
            group( "Fun" );

        LocalDateTime now = new LocalDateTime();
        Option<FileAttributes> none = Option.<FileAttributes>none();

        unixPackage.
            addFile( pomXml, regularFile( fromString( "/pom.xml" ), now, 0, none ) ).
            addFile( fooLicense, regularFile( fromString( "/foo-license.txt" ), now, 0, none ) ).
            addFile( barLicense, regularFile( fromString( "/bar-license.txt" ), now, 0, none ) );

        unixPackage.
            debug( true ).
            packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }
}
