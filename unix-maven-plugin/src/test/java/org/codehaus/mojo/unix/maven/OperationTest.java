package org.codehaus.mojo.unix.maven;

import junit.framework.TestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.plexus.PlexusTestCase;
import org.easymock.MockControl;

import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class OperationTest
    extends TestCase
{
    public void testExtractWithPattern()
        throws IOException, MojoFailureException
    {
        String archivePath = PlexusTestCase.getTestPath( "src/test/resources/operation/extract.jar" );

        FileSystemManager fsManager = VFS.getManager();
        FileObject archiveObject = fsManager.resolveFile( archivePath );
        FileObject archive = fsManager.createFileSystem( archiveObject );
        FileObject fooLicense = archive.getChild( "foo-license.txt" );
        FileObject barLicense = archive.getChild( "mydir" ).getChild( "bar-license.txt" );

        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        control.expectAndReturn( fileCollector.addFile( fooLicense, "licenses/foo-license.txt", "nobody", "nogroup", "0644" ), fileCollector );
        control.expectAndReturn( fileCollector.addFile( barLicense, "licenses/bar-license.txt", "nobody", "nogroup", "0644" ), fileCollector );
        control.replay();

        Extract extract = new Extract();
        extract.setArchive( archivePath );
        extract.setToDir( "licenses" );
        extract.setPattern( ".*/(.*license.*)" );
        extract.setReplacement( "$1" );
        extract.perform( new Defaults(), fileCollector );
        control.verify();
    }
}
