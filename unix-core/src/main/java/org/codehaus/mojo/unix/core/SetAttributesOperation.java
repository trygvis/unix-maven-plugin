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

import fj.F2;
import static fj.Function.curry;
import fj.data.Option;
import static fj.data.Option.none;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;
import org.codehaus.mojo.unix.util.vfs.IncludeExcludeFileSelector;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributesOperation
    extends AssemblyOperation
{
    private final RelativePath basedir;

    private final Option<FileAttributes> fileAttributes;

    private final Option<FileAttributes> directoryAttributes;

    private final IncludeExcludeFileSelector selector;

    public SetAttributesOperation( RelativePath basedir, List<String> includes, List<String> excludes,
                                   Option<FileAttributes> fileAttributes, Option<FileAttributes> directoryAttributes )
    {
        validateNotNull( basedir, includes, excludes, fileAttributes, directoryAttributes );
        this.basedir = basedir;
        this.fileAttributes = fileAttributes;
        this.directoryAttributes = directoryAttributes;

        selector = IncludeExcludeFileSelector.build( null ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        if ( fileAttributes.isSome() )
        {
            fileCollector.applyOnFiles( curry( applyAttributes, fileAttributes ) );
        }

        if ( directoryAttributes.isSome() )
        {
            fileCollector.applyOnDirectories( curry( applyAttributes, directoryAttributes ) );
        }
    }

    F2<Option<FileAttributes>, RelativePath, Option<FileAttributes>> applyAttributes =
        new F2<Option<FileAttributes>, RelativePath, Option<FileAttributes>>()
        {
            public Option<FileAttributes> f( Option<FileAttributes> fileAttributes, RelativePath path )
            {
                if ( path.startsWith( basedir ) &&
                    selector.matches( path.string.substring( basedir.string.length() ) ) )
                {
                    return fileAttributes;
                }

                return none();
            }
        };
}
