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
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;

import static java.util.Arrays.*;
import java.util.*;
import java.util.List;

import fj.*;
import static fj.P.*;
import fj.data.*;
import static fj.data.Option.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractFileSetOp
    extends AssemblyOp
{
    private RelativePath to = RelativePath.BASE;

    private List<String> includes = Collections.emptyList();

    private List<String> excludes = Collections.emptyList();

    private String pattern;

    private String replacement;

    private MojoFileAttributes fileAttributes = new MojoFileAttributes();

    private MojoFileAttributes directoryAttributes = new MojoFileAttributes();

    public AbstractFileSetOp( String operationType )
    {
        super( operationType );
    }

    public void setTo( String to )
    {
        this.to = relativePath( to );
    }

    public void setIncludes( String[] includes )
    {
        this.includes = asList( includes );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = asList( excludes );
    }

    public void setPattern( String pattern )
    {
        this.pattern = nullifEmpty( pattern );
    }

    public void setReplacement( String replacement )
    {
        this.replacement = nullifEmpty( replacement );
    }

    public void setFileAttributes( MojoFileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public void setDirectoryAttributes( MojoFileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }

    protected AssemblyOperation createOperationInternal( FileObject archive, FileAttributes defaultFileAttributes,
                                                         FileAttributes defaultDirectoryAttributes )
        throws MojoFailureException, FileSystemException
    {
        Option<P2<String, String>> pattern = none();
        if ( this.pattern != null )
        {
            if ( replacement == null )
            {
                throw new MojoFailureException( "A replacement expression has to be set if a pattern is given." );
            }

            pattern = some( p( this.pattern, replacement ) );
        }

        return new CopyDirectoryOperation( archive, to, includes, excludes, pattern,
                                           defaultFileAttributes.useAsDefaultsFor( fileAttributes.create() ),
                                           defaultDirectoryAttributes.useAsDefaultsFor( directoryAttributes.create() ) );
    }
}
