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

import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.Validate.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SymlinkOperation
    extends AssemblyOperation
{
    private final RelativePath path;

    private final String target;

    private final FileAttributes attributes;

    public SymlinkOperation( RelativePath path, String target, FileAttributes attributes )
    {
        validateNotNull( path, target, attributes );
        this.path = path;
        this.target = target;
        this.attributes = attributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        fileCollector.addSymlink( symlink( path, new LocalDateTime(), some( attributes ), target ) );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Symlink:" ).
            add( " Path: " + path ).
            add( " Target: " + target ).
            add( " Attributes: " + attributes );
    }
}
