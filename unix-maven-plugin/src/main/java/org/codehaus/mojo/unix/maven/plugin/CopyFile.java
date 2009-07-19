package org.codehaus.mojo.unix.maven.plugin;

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
import org.apache.maven.plugin.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyFile
    extends AssemblyOp
{
    private File path;

    private RelativePath toFile;

    private RelativePath toDir;

    private MojoFileAttributes attributes = new MojoFileAttributes();

    public CopyFile()
    {
        super( "copy-file" );
    }

    public void setToFile( String toFile )
    {
        this.toFile = relativePath( toFile );
    }

    public void setToDir( String toDir )
    {
        this.toDir = relativePath( toDir );
    }

    public AssemblyOperation createOperation( FileObject basedir, FileAttributes defaultFileAttributes,
                                              FileAttributes defaultDirectoryAttributes )
        throws MojoFailureException, FileSystemException
    {
        return new CopyFileOperation( defaultFileAttributes.useAsDefaultsFor( attributes.create() ),
                                      resolve( basedir.getFileSystem(), path ),
                                      validateAndResolveOutputFile( path, toDir, this.toFile ) );
    }
}
