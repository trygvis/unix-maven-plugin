package org.codehaus.mojo.unix.core;

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

import static fj.data.Option.*;
import fj.data.*;
import fj.*;
import static fj.P.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.relativePath;
import org.codehaus.plexus.*;
import org.easymock.*;

import static java.util.Arrays.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class OperationTest
    extends PlexusTestCase
{
    public final static FileAttributes fileAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0755 ) );

    public final static FileAttributes directoryAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0644 ) );

    private static FileObject baseFileObject;

    public static FileObject getBaseFileObject()
    {
        try
        {
            if ( baseFileObject == null )
            {
                FileSystemManager fsManager = VFS.getManager();
                baseFileObject = fsManager.resolveFile( PlexusTestCase.getBasedir() );
            }

            return baseFileObject;
        }
        catch ( FileSystemException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static final Paths paths = new Paths();
    public static final Files files = new Files();
    public static final Objects objects = new Objects();

    public static class Paths {
        RelativePath optJettyBin = relativePath( "/opt/jetty/bin" );
        RelativePath optJettyBinExtraApp = relativePath( "/opt/jetty/bin/extra-app" );
        RelativePath optJettyReadmeUnix = relativePath( "/opt/jetty/README-unix.txt" );
        RelativePath optJettyBashProfile = relativePath( "/opt/jetty/.bash_profile" );
    }

    public static class Files {
        final FileObject files = resolveFile( getBaseFileObject(), "src/test/resources/operation/files" );
        final FileObject optJettyReadmeUnix = resolveFile( files, paths.optJettyReadmeUnix.string );
        final FileObject optJettyBinExtraApp = resolveFile( files, paths.optJettyBinExtraApp.string );
        final FileObject optJettyBashProfile = resolveFile( files, paths.optJettyBashProfile.string );

        private FileObject resolveFile( FileObject files, String string )
        {
            try
            {
                return files.resolveFile( string );
            }
            catch ( FileSystemException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    public static class Objects {
        UnixFsObject.Directory optJettyBin = createDirectory( "opt/jetty/bin", files.files, directoryAttributes );
        UnixFsObject.Directory optJetty = createDirectory( "opt/jetty/", files.files, directoryAttributes );
        UnixFsObject.Directory opt = createDirectory( "opt/", files.files, directoryAttributes );
        UnixFsObject.Directory base = createDirectory( ".", files.files, directoryAttributes );
        UnixFsObject.RegularFile optJettyBashProfile = fromFileObject( paths.optJettyBashProfile, files.optJettyBashProfile, fileAttributes );
        UnixFsObject.RegularFile optJettyBinExtraApp = fromFileObject( paths.optJettyBinExtraApp, files.optJettyBinExtraApp, fileAttributes );
        UnixFsObject.RegularFile optJettyReadmeUnix = fromFileObject( paths.optJettyReadmeUnix, files.optJettyReadmeUnix, fileAttributes );
    }

    /**
     * Based on the <code>&gt;copy&lt;</code> operation that is in the jetty IT.
     */
    public void testCopyOnACompleteDirectoryStructure()
        throws Exception
    {
        assertEquals( FileType.FOLDER, files.files.getType() );
        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        fileCollector.addFile( files.optJettyBinExtraApp, objects.optJettyBinExtraApp );
        control.setMatcher( new FileObjectMatcher() );
        control.setReturnValue( fileCollector );

        fileCollector.addFile( files.optJettyReadmeUnix, objects.optJettyReadmeUnix  );
        control.setReturnValue( fileCollector );

        fileCollector.addFile( files.optJettyBashProfile, objects.optJettyBashProfile );
        control.setReturnValue( fileCollector );

        control.expectAndReturn( fileCollector.addDirectory( objects.optJettyBin ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.optJetty ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.opt ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.base ), fileCollector );
        control.replay();

        new CopyDirectoryOperation( files.files, RelativePath.BASE, null, null, Option.<P2<String, String>>none(),
                                    fileAttributes, directoryAttributes ).
            perform( fileCollector );

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
        UnixFsObject.RegularFile fooLicenseUnixFile =
            fromFileObject( relativePath( "licenses/foo-license.txt" ), fooLicense, fileAttributes );

        FileObject barLicense = archive.getChild( "mydir" ).getChild( "bar-license.txt" );
        UnixFsObject.RegularFile barLicenseUnixFile =
            fromFileObject( relativePath( "licenses/bar-license.txt" ), barLicense, fileAttributes );

        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        control.expectAndReturn( fileCollector.addFile( barLicense, barLicenseUnixFile ), fileCollector );
        control.expectAndReturn( fileCollector.addFile( fooLicense, fooLicenseUnixFile ), fileCollector );
        control.replay();

        new CopyDirectoryOperation( archive, relativePath( "licenses" ), asList( "**/*license.txt" ), null,
                                    some( p(".*/(.*license.*)", "$1")), fileAttributes, directoryAttributes ).
            perform( fileCollector );

        control.verify();
    }

    static class FileObjectMatcher
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

            return MockControl.EQUALS_MATCHER.matches( new Object[]{expected.getName()},
                                                       new Object[]{actual.getName()} );
        }
    }

    private static UnixFsObject.Directory createDirectory( String path, FileObject files, FileAttributes directoryAttributes )
    {
        try
        {
            return AssemblyOperationUtil.dirFromFileObject( relativePath( path ), files.resolveFile( path ), directoryAttributes );
        }
        catch ( FileSystemException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static UnixFsObject.RegularFile fromFileObject( RelativePath path, FileObject file,
                                                            FileAttributes attributes )
    {
        try
        {
            return AssemblyOperationUtil.fromFileObject( path, file, attributes );
        }
        catch ( FileSystemException e )
        {
            throw new RuntimeException( e );
        }
    }
}
