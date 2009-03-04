package org.codehaus.mojo.unix.pkg.prototype;

import fj.F;
import fj.data.Option;
import static fj.data.Option.some;
import junit.framework.TestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.codehaus.mojo.unix.FileAttributes;
import static org.codehaus.mojo.unix.UnixFileMode._0644;
import static org.codehaus.mojo.unix.UnixFileMode._0755;
import org.codehaus.mojo.unix.UnixFsObject;
import static org.codehaus.mojo.unix.UnixFsObject.regularFile;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import static org.codehaus.mojo.unix.util.RelativePath.BASE;
import static org.codehaus.mojo.unix.util.UnixUtil.getTestPath;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.joda.time.LocalDateTime;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFileTest
    extends TestCase
{
    private final LocalDateTime dateTime = new LocalDateTime();

    RelativePath extractJarPath = fromString( "extract.jar" );
    RelativePath bashProfilePath = fromString( "/opt/jetty/.bash_profile" );

    FileAttributes fileAttributes = new FileAttributes( some( "nouser" ), some( "nogroup" ), some( _0644 ) );
    FileAttributes dirAttributes = new FileAttributes( some( "nouser" ), some( "nogroup" ), some( _0755 ) );
    RelativePath specialPath = fromString( "/special" );

    public void testBasic()
        throws Exception
    {
        FileSystemManager fsManager = VFS.getManager();

        FileObject root = fsManager.resolveFile( getTestPath( "target/prototype-test/assembly" ) );
        root.createFolder();

        PrototypeFile prototypeFile = new PrototypeFile();

        FileObject bashProfileObject = fsManager.resolveFile( getTestPath( "src/test/non-existing/bash_profile" ) );
        FileObject extractJarObject = fsManager.resolveFile( getTestPath( "src/test/non-existing/extract.jar" ) );
        UnixFsObject.RegularFile extractJar = regularFile( extractJarPath, dateTime, 0, some( fileAttributes ) );
        UnixFsObject.RegularFile bashProfile = regularFile( bashProfilePath, dateTime, 0, some( fileAttributes ) );

        prototypeFile.addFile( bashProfileObject, bashProfile );
        prototypeFile.addFile( extractJarObject, extractJar );
        prototypeFile.addDirectory( UnixFsObject.directory( BASE, dateTime, dirAttributes ) );
        prototypeFile.addDirectory( UnixFsObject.directory( specialPath, dateTime, dirAttributes ) );
        prototypeFile.applyOnFiles( filter( extractJarPath, fileAttributes.user( "funnyuser" ) ) );
        prototypeFile.applyOnDirectories( filter( specialPath, dirAttributes.group( "funnygroup" ) ) );

        LineFile stream = new LineFile();

        prototypeFile.streamTo( stream );

        assertEquals( new LineFile().
            add( "f none /opt/jetty/.bash_profile=" + bashProfileObject.getName().getPath() + " 0644 nouser nogroup" ).
            add( "d none /special 0755 nouser funnygroup" ).
            add( "d none / 0755 nouser nogroup" ).
            add( "f none /extract.jar=" + extractJarObject.getName().getPath() + " 0644 funnyuser nogroup" ).
            toString(), stream.toString() );
    }

    private F<RelativePath, Option<FileAttributes>> filter( final RelativePath filteredPath, final FileAttributes newAttributes )
    {
        return new F<RelativePath, Option<FileAttributes>>()
        {
            public Option<FileAttributes> f( RelativePath path )
            {
                return !path.equals( filteredPath ) ? Option.<FileAttributes>none() : some( newAttributes );
            }
        };
    }
}
