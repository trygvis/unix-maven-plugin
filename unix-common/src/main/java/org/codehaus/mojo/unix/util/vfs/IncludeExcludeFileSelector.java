package org.codehaus.mojo.unix.util.vfs;

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
import static org.apache.commons.vfs.FileType.*;
import org.codehaus.mojo.unix.io.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class IncludeExcludeFileSelector
    implements FileSelector
{
    private final FileName root;

    private final boolean filesOnly;

    private final IncludeExcludeFilter filter;

    public IncludeExcludeFileSelector( FileName root, IncludeExcludeFilter filter )
    {
        this.root = root;
        filesOnly = false;
        this.filter = filter;
    }

    public IncludeExcludeFileSelector( FileName root, boolean filesOnly, IncludeExcludeFilter filter )
    {
        this.root = root;
        this.filesOnly = filesOnly;
        this.filter = filter;
    }

    public boolean includeFile( FileSelectInfo fileSelectInfo )
        throws Exception
    {
        FileObject fileObject = fileSelectInfo.getFile();
        FileName name = fileObject.getName();

        if ( filesOnly && fileObject.getType() != FILE )
        {
            return false;
        }

        String relativePath = root.getRelativeName( name );

        return filter.matches( relativePath( relativePath ) );
    }

    public boolean traverseDescendents( FileSelectInfo fileSelectInfo )
        throws Exception
    {
        return true;
    }
}
