package org.codehaus.mojo.unix.maven.pkg.prototype;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFileTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        FileSystemManager fsManager = VFS.getManager();

        FileObject root = fsManager.resolveFile( getTestPath( "target/prototype-test/assembly" ) );
        root.createFolder();

        PrototypeFile prototypeFile = new PrototypeFile( root );

        FileAttributes fileAttributes = new FileAttributes( "nouser", "nogroup", UnixFileMode._0644 );
        FileAttributes dirAttributes = new FileAttributes( "nouser", "nogroup", UnixFileMode._0755 );

        String archivePath = PlexusTestCase.getTestPath( "src/test/resources/operation/extract.jar" );
        FileObject archive = fsManager.resolveFile( archivePath );
        prototypeFile.addFile( archive, RelativePath.fromString( "/extract.jar" ), fileAttributes );
        prototypeFile.addFile( archive, RelativePath.fromString( "/opt/jetty/.bash_profile" ), fileAttributes );
        prototypeFile.addDirectory( RelativePath.fromString( "." ), dirAttributes );

//        assertTrue( prototypeFile.hasPath( "/" ) );

        assertEquals( new LineFile().
            add( "d none / 0755 nouser nogroup" ).
            add( "f none /extract.jar=" + archivePath + " 0644 nouser nogroup" ).
            add( "f none /opt/jetty/.bash_profile=" + archivePath + " 0644 nouser nogroup" ).
            toString(), prototypeFile.toLineFile().toString() );
    }
}
