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

import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.line.LineStreamUtil.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.vfs.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import fj.*;
import fj.data.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyDirectoryOperation
    extends AssemblyOperation
{
    private final FileObject from;

    private final RelativePath to;

    private final List<String> includes;

    private final List<String> excludes;

    private final Option<P2<String, String>> pattern;

    private final FileAttributes fileAttributes;

    private final FileAttributes directoryAttributes;

    public CopyDirectoryOperation( FileObject from, RelativePath to, List<String> includes, List<String> excludes,
                                   Option<P2<String, String>> pattern, FileAttributes fileAttributes,
                                   FileAttributes directoryAttributes )
    {
        this.from = from;
        this.to = to;
        this.includes = includes;
        this.excludes = excludes;
        this.pattern = pattern;
        this.fileAttributes = fileAttributes;
        this.directoryAttributes = directoryAttributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        Pattern pattern = this.pattern.isSome() ? Pattern.compile( this.pattern.some()._1() ) : null;

        IncludeExcludeFileSelector selector = IncludeExcludeFileSelector.build( from.getName() ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        List<FileObject> files = new ArrayList<FileObject>();
        from.findFiles( selector, true, files );

        for ( FileObject f : files )
        {
            if ( f.getName().getBaseName().equals( "" ) )
            {
                continue;
            }

            String relativeName = from.getName().getRelativeName( f.getName() );

            // Transform the path if the pattern is set. The input path will always have a leading slash
            // to make it possible to write more natural expressions.
            // With this one can write "/server-1.0.0/(.*)" => $1
            if ( pattern != null )
            {
                String path = relativePath( relativeName ).asAbsolutePath( "/" );
                relativeName = pattern.matcher( path ).replaceAll( this.pattern.some()._2() );
            }

            if ( f.getType() == FileType.FILE )
            {
                fileCollector.addFile( f, AssemblyOperationUtil.fromFileObject( to.add( relativeName ), f, fileAttributes ) );
            }
            else if ( f.getType() == FileType.FOLDER )
            {
                fileCollector.addDirectory( AssemblyOperationUtil.dirFromFileObject( to.add( relativeName ), f, directoryAttributes ) );
            }
        }
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Copy directory:" ).
            add( " From: " + asFile( from ).getAbsolutePath() ).
            add( " To: " + to );
        if ( !includes.isEmpty() )
        {
            streamWriter.add( " Includes: ").
                addAllLines( prefix( includes, "  " ) );
        }
        else
        {
            streamWriter.add( " No includes set" );
        }

        if ( !excludes.isEmpty() )
        {
            streamWriter.add( " Excludes: " ).
                addAllLines( prefix( excludes, "  " ) );
        }
        else
        {
            streamWriter.add( " No excludes set" );
        }

        streamWriter.add( pattern.map(new F<P2<String, String>, String>()
        {
            public String f( P2<String, String> pattern )
            {
                return " Pattern: " + pattern._1() + ", replacement: " + pattern._2();
            }
        } ).orSome( " Pattern: not set" ) );

        streamWriter.add( " Attributes:" ).
            add( " File     : " + fileAttributes ).
            add( " Directory: " + directoryAttributes );
    }
}
