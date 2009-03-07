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
import fj.P;
import static fj.P.p;
import fj.P2;
import fj.data.HashMap;
import fj.data.List;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.some;
import fj.pre.Ord;
import fj.pre.Ordering;
import org.apache.commons.vfs.FileObject;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.UnixUtil;
import static org.codehaus.mojo.unix.util.UnixUtil.noneString;
import static org.codehaus.mojo.unix.util.UnixUtil.someE;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.asFile;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeFile
    implements LineProducer
{
    private List<String> iFiles = nil();

    private HashMap<RelativePath, P2<? extends UnixFsObject, ? extends PrototypeEntry>> entries = HashMap.hashMap();

    private List<F<RelativePath, Option<FileAttributes>>> fileAttributeF = nil();

    private List<F<RelativePath, Option<FileAttributes>>> directoryAttributeF = nil();

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
        return entries.contains( path );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        DirectoryEntry entry = new DirectoryEntry( Option.<String>none(), directory.path, someE(directory.attributes, "Attributes is not set." ) );

        entries.set( directory.path, p( (UnixFsObject)directory, (PrototypeEntry) entry ) );
    }

    public void addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        // TODO: add missing parent directory entries

        Option<File> from = some( asFile( fromFile ) );
        FileEntry entry = new FileEntry( noneString, file.path, some( false ), from, someE( file.attributes, "Attributes is not set." ) );
        entries.set( file.path, p( (UnixFsObject)file, (PrototypeEntry) entry ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        entries.set( symlink.path, p( symlink, new SymlinkEntry( noneString, symlink.path, symlink.target ) ));
    }

    public void applyOnFiles( F<RelativePath, Option<FileAttributes>> f )
    {
        fileAttributeF = fileAttributeF.cons( f );
    }

    public void applyOnDirectories( F<RelativePath, Option<FileAttributes>> f )
    {
        directoryAttributeF = directoryAttributeF.cons( f );
    }

    public void streamTo( LineStreamWriter stream )
    {
        stream.
            addAllLines( iFiles.reverse() );

        for ( P2<? extends UnixFsObject, ? extends PrototypeEntry> p2 : entries.values() )
        {
            PrototypeEntry entry = p2._2();

            Option<UnixFsObject> fileOption = some( p2._1() ).
                filter( UnixUtil.Filter.<UnixFsObject>instanceOfFilter( UnixFsObject.RegularFile.class ) );

            if ( fileOption.isSome() )
            {
                UnixFsObject regularFile = fileOption.some();

                // I have no idea why I'm getting a warning here -- trygve
                Option<FileAttributes> q = regularFile.attributes;
                P2<RelativePath, FileAttributes> result = fileAttributeF.
                    reverse().
                    foldLeft( fileAttributeFolder, P.p( regularFile.path, q.some() ) );

                entry = ((FileEntry)entry).setFileAttributes( result._2() );
            }

            Option<UnixFsObject> directoryOption = some( p2._1() ).
                filter( UnixUtil.Filter.<UnixFsObject>instanceOfFilter( UnixFsObject.Directory.class ) );

            if ( directoryOption.isSome() )
            {
                UnixFsObject directory = directoryOption.some();

                // I have no idea why I'm getting a warning here -- trygve
                Option<FileAttributes> q = directory.attributes;
                P2<RelativePath, FileAttributes> result = directoryAttributeF.
                    reverse().
                    foldLeft( fileAttributeFolder, P.p( directory.path, q.some() ) );

                entry = ((DirectoryEntry)entry).setFileAttributes( result._2() );
            }

            entry
                .streamTo( stream );
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
