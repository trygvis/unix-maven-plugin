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
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class AssemblyOperationUtil
{
    public static UnixFsObject.RegularFile fromFileObject( RelativePath toFile, FileObject fromFile,
                                                           FileAttributes attributes )
        throws FileSystemException
    {
        FileContent content = fromFile.getContent();

        LocalDateTime time = new LocalDateTime( content.getLastModifiedTime() );

        return regularFile( toFile, time, content.getSize(), fromNull( attributes ) );
    }

    public static UnixFsObject.Directory dirFromFileObject( RelativePath toFile, FileObject fromFile,
                                                            FileAttributes attributes )
        throws FileSystemException
    {
        if ( !fromFile.getType().equals( FileType.FOLDER ) )
        {
            throw new FileSystemException( "Not a directory: " + fromFile.getName().getPath() + ", was: " +
                fromFile.getType() );
        }

        FileContent content = fromFile.getContent();

        return UnixFsObject.directory( toFile, new LocalDateTime( content.getLastModifiedTime() ), attributes );
    }
}
