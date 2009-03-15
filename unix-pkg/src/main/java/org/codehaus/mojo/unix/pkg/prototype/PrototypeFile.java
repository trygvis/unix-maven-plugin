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

import fj.F;
import fj.F2;
import fj.P2;
import fj.data.List;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.some;
import fj.pre.Ord;
import fj.pre.Ordering;
import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.PackageFileSystem;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.UnixUtil.noneString;
import static org.codehaus.mojo.unix.util.UnixUtil.someE;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.asFile;

import java.io.File;
import java.util.Comparator;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFile
    implements LineProducer
{
    private List<String> iFiles = nil();

    private final PackageFileSystem<PrototypeEntry> fileSystem = new PackageFileSystem<PrototypeEntry>( new Comparator<PrototypeEntry>()
    {
        public int compare( PrototypeEntry o1, PrototypeEntry o2 )
        {
            return o1.getPath().compareTo( o2.getPath() );
        }
    } );

    public void addIFileIf( File file, String name )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        iFiles = iFiles.cons( "i " + name + "=" + file.getAbsolutePath() );
    }

    public void addIFileIf( File file )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        addIFileIf( file, file.getName() );
    }

    public boolean hasPath( RelativePath path )
    {
        return fileSystem.hasPath( path );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        DirectoryEntry entry = new DirectoryEntry( Option.<String>none(), directory.path, someE(directory.attributes, "Attributes is not set." ) );

        fileSystem.addDirectory( directory, entry );
    }

    public void addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        // TODO: add missing parent directory entries

        Option<File> from = some( asFile( fromFile ) );
        FileEntry entry = new FileEntry( noneString, file.path, some( false ), from, someE( file.attributes, "Attributes is not set." ) );
        fileSystem.addFile( file, entry );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        fileSystem.addSymlink( symlink, new SymlinkEntry( noneString, symlink.path, symlink.value ) );
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        fileSystem.apply( f );
    }

    public void streamTo( final LineStreamWriter stream )
    {
        stream.
            addAllLines( iFiles.reverse() );

        for ( PrototypeEntry entry : fileSystem )
        {
            entry.streamTo( stream );
        }
    }

    // Applies the function to each path, and selects the last one that was some()
    F2<P2<RelativePath, FileAttributes>, F<RelativePath, Option<FileAttributes>>, P2<RelativePath, FileAttributes>> fileAttributeFolder =
        new F2<P2<RelativePath, FileAttributes>, F<RelativePath, Option<FileAttributes>>, P2<RelativePath, FileAttributes>>()
    {
        public P2<RelativePath, FileAttributes> f( final P2<RelativePath, FileAttributes> previous,
                                                   final F<RelativePath, Option<FileAttributes>> transformer )
        {
            return previous.map2( new F<FileAttributes, FileAttributes>()
            {
                public FileAttributes f( FileAttributes fileAttributes )
                {
                    return transformer.f( previous._1() ).orSome( previous._2() );
                }
            } );
        }
    };

    Ord<P2<UnixFsObject, PrototypeEntry>> entriesOrd =
        Ord.ord( new F<P2<UnixFsObject, PrototypeEntry>, F<P2<UnixFsObject, PrototypeEntry>, Ordering>>()
        {
            public F<P2<UnixFsObject, PrototypeEntry>, Ordering> f( final P2<UnixFsObject, PrototypeEntry> a )
            {
                return new F<P2<UnixFsObject, PrototypeEntry>, Ordering>()
                {
                    public Ordering f( P2<UnixFsObject, PrototypeEntry> b )
                    {
                        return RelativePath.ord.compare( a._1().path, b._1().path );
                    }
                };
            }
        } );
}
