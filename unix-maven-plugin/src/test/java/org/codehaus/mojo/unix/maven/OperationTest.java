package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;
import org.codehaus.plexus.PlexusTestCase;
import org.easymock.AbstractMatcher;
import org.easymock.MockControl;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class OperationTest
    extends PlexusTestCase
{
    public void testDefaults()
    {
        assertEquals( Defaults.DEFAULT_FILE_ATTRIBUTES, Defaults.DEFAULT_FILE_ATTRIBUTES );

        Defaults defaults = new Defaults();
        assertEquals( new FileAttributes().create(), defaults.getFileAttributes() );

        org.codehaus.mojo.unix.FileAttributes fileAttributes =
            Defaults.DEFAULT_FILE_ATTRIBUTES.
                useAsDefaultsFor( defaults.getFileAttributes() );

        // Without any adjustments, the value should be exactly like the default defaults
        assertEquals( Defaults.DEFAULT_FILE_ATTRIBUTES, fileAttributes );

        System.out.println( "fileAttributes = " + fileAttributes );
    }
    
    public void testRegexp()
    {
        String pattern = "/jetty-7.0.0pre3/LICENSES/(.*)";
        String replacement = "share/licenses/$1";
        String path = "/jetty-7.0.0pre3/LICENSES/ccla-exist.pdf";

        assertEquals( "share/licenses/ccla-exist.pdf", path.replaceAll( pattern, replacement ) );
        assertEquals( "share/licenses/ccla-exist.pdf", Pattern.compile( pattern ).matcher( path ).replaceAll( replacement ) );
    }

    /**
     * Based on the <code>&gt;copy&lt;</code> operation that is in the jetty IT.
     */
    public void testCopyOnACompleteDirectoryStructure()
        throws Exception
    {
        FileSystemManager fsManager = VFS.getManager();
        FileObject basedir = fsManager.resolveFile( getTestPath( "." ) );
        FileObject files = basedir.resolveFile( "src/test/resources/operation/files" );

        Defaults defaults = new Defaults();
        org.codehaus.mojo.unix.FileAttributes fileAttributes =
            Defaults.DEFAULT_FILE_ATTRIBUTES.
                useAsDefaultsFor( defaults.getFileAttributes() );

        org.codehaus.mojo.unix.FileAttributes directoryAttributes =
            Defaults.DEFAULT_DIRECTORY_ATTRIBUTES.
                useAsDefaultsFor( defaults.getDirectoryAttributes() );

        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();
        RelativePath path;
        path = RelativePath.fromString( "/opt/jetty/README-unix.txt" );
        FileObject object = files.resolveFile( path.string );
        fileCollector.addFile( object, path, fileAttributes );
        control.setMatcher( new FileObjectMatcher() );
        control.setReturnValue( fileCollector );

        path = RelativePath.fromString( "/opt/jetty/bin/extra-app" );
        fileCollector.addFile( files.resolveFile( path.string ), path, fileAttributes );
        control.setReturnValue( fileCollector );

        path = RelativePath.fromString( "/opt/jetty/.bash_profile" );
        fileCollector.addFile( files.resolveFile( path.string ), path, fileAttributes );
        control.setReturnValue( fileCollector );

        control.expectAndReturn( fileCollector.addDirectory( RelativePath.fromString( "/opt/jetty/bin" ), directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( RelativePath.fromString( "/opt/jetty/" ), directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( RelativePath.fromString( "/opt" ), directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( RelativePath.fromString( "/" ), directoryAttributes ), fileCollector );
        control.replay();

        Copy copy = new Copy();
        copy.setPath( VfsUtil.asFile( files ) );
        copy.setToDir( "/" );
        copy.perform( basedir, defaults, fileCollector );
        control.verify();
    }

    public void testExtractWithPattern()
        throws Exception
    {
        String archivePath = PlexusTestCase.getTestPath( "src/test/resources/operation/extract.jar" );

        FileSystemManager fsManager = VFS.getManager();
        FileObject archiveObject = fsManager.resolveFile( archivePath );
        assertEquals( FileType.FILE, archiveObject.getType() );
        FileObject archive = fsManager.createFileSystem( archiveObject );
        FileObject fooLicense = archive.getChild( "foo-license.txt" );
        FileObject barLicense = archive.getChild( "mydir" ).getChild( "bar-license.txt" );

        Defaults defaults = new Defaults();
        org.codehaus.mojo.unix.FileAttributes fileAttributes =
            Defaults.DEFAULT_FILE_ATTRIBUTES.
                useAsDefaultsFor( defaults.getFileAttributes() );

        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        control.expectAndReturn( fileCollector.addFile( fooLicense, RelativePath.fromString( "licenses/foo-license.txt" ), fileAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.addFile( barLicense, RelativePath.fromString( "licenses/bar-license.txt" ), fileAttributes ), fileCollector );
        control.replay();

        Extract extract = new Extract();
        extract.setArchive( archivePath );
        extract.setToDir( "licenses" );
        extract.setIncludes( Arrays.asList( new String[]{"**/*license.txt"} ) );
        extract.setPattern( ".*/(.*license.*)" );
        extract.setReplacement( "$1" );
        extract.perform( fsManager.getBaseFile(), defaults, fileCollector );
        control.verify();
    }

    private static class FileObjectMatcher
        extends AbstractMatcher
    {
        int i = 0;

        protected boolean argumentMatches( Object e, Object a )
        {
            i++;
            if ( i != 1 )
            {
                return super.argumentMatches( e, a );
            }

            FileObject expected = (FileObject) e;
            FileObject actual = (FileObject) a;

            return MockControl.EQUALS_MATCHER.matches(
                new Object[]{expected.getName()},
                new Object[]{actual.getName()}
            );
        }
    }
}
