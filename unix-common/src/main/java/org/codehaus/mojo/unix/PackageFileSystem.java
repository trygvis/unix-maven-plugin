package org.codehaus.mojo.unix;

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
import fj.P2;
import fj.data.Option;
import org.codehaus.mojo.unix.util.RelativePath;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageFileSystem<A extends HasFileAttributes<A>>
    implements Iterable<A>
{
    private LinkedList<P2<UnixFsObject, A>> entries = new LinkedList<P2<UnixFsObject, A>>();

    private Comparator<? super A> comparator;

    public PackageFileSystem( Comparator<? super A> comparator )
    {
        this.comparator = comparator;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public boolean hasPath( final RelativePath path )
    {
        for ( P2<UnixFsObject, A> entry : entries )
        {
            if ( entry._1().path.equals( path ) )
            {
                return true;
            }
        }

        return false;
    }

    public void addDirectory( UnixFsObject.Directory directory, A a )
    {
        entries.add( P.p( (UnixFsObject) directory, a ) );
    }

    public void addFile( UnixFsObject.RegularFile file, A a )
    {
        entries.add( P.p( (UnixFsObject) file, a ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink, A a )
    {
        entries.add( P.p( (UnixFsObject) symlink, a ) );
    }

    public void apply( final F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        ListIterator<P2<UnixFsObject, A>> listIterator = entries.listIterator();

        while ( listIterator.hasNext() )
        {
            P2<UnixFsObject, A> entry = listIterator.next();

            FileAttributes attributes = f.f( entry._1(), entry._2().getFileAttributes() );

            // TODO: check if the attributes was modified
            listIterator.set( P.p( entry._1(), entry._2().setFileAttributes( attributes ) ) );
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    // Applies the function to each path, and selects the last one that was some()

    F2<P2<RelativePath, FileAttributes>, F<RelativePath, Option<FileAttributes>>, P2<RelativePath, FileAttributes>>
        fileAttributeFolder =
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

    public Iterator<A> iterator()
    {
        TreeSet<A> files = new TreeSet<A>( comparator );

        for ( P2<UnixFsObject, A> entry : entries )
        {
            files.remove( entry._2() );
            files.add( entry._2() );
        }

        return files.iterator();
    }
}
