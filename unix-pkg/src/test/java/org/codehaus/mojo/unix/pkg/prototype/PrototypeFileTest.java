package org.codehaus.mojo.unix.pkg.prototype;

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
import static org.codehaus.mojo.unix.util.RelativePath.BASE;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
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
        prototypeFile.apply( filter( extractJarPath, fileAttributes.user( "funnyuser" ) ) );
        prototypeFile.apply( filter( specialPath, dirAttributes.group( "funnygroup" ) ) );

        LineFile stream = new LineFile();

        prototypeFile.streamTo( stream );

        assertEquals( new LineFile().
            add( "d none / 0755 nouser nogroup" ).
            add( "f none /extract.jar=" + extractJarObject.getName().getPath() + " 0644 funnyuser nogroup" ).
            add( "f none /opt/jetty/.bash_profile=" + bashProfileObject.getName().getPath() + " 0644 nouser nogroup" ).
            add( "d none /special 0755 nouser funnygroup" ).
            toString(), stream.toString() );
    }

    private F2<UnixFsObject, FileAttributes, FileAttributes> filter( final RelativePath s, final FileAttributes newAttributes )
    {
        return new F2<UnixFsObject, FileAttributes, FileAttributes>()
        {
            public FileAttributes f( UnixFsObject fsObject, FileAttributes attributes )
            {
                return !fsObject.path.string.startsWith( s.string ) ? attributes : attributes.useAsDefaultsFor( newAttributes );
            }
        };
    }
}
