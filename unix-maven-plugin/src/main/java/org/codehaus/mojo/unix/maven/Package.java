package org.codehaus.mojo.unix.maven;

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

import fj.data.*;
import static fj.data.Option.*;
import static fj.data.List.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Package
{
    public Option<String> classifier = none();

    public Option<String> id = none();

    public Option<String> name = none();

    public Option<String> description = none();

    public MojoFileAttributes fileAttributes = new MojoFileAttributes();

    public MojoFileAttributes directoryAttributes = new MojoFileAttributes();

    public List<AssemblyOp> assembly = nil();

    public void setClassifier( String classifier )
    {
        this.classifier = Option.fromNull( classifier );
    }

    public void setId( String id )
    {
        this.id = Option.fromNull( id );
    }

    public void setName( String name )
    {
        this.name = Option.fromNull( name );
    }

    public void setDescription( String description )
    {
        this.description = Option.fromNull( description );
    }

    public void setFileAttributes( MojoFileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public void setDirectoryAttributes( MojoFileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }

    public void setAssembly( AssemblyOp[] assembly )
    {
        this.assembly = list( assembly );
    }
}
