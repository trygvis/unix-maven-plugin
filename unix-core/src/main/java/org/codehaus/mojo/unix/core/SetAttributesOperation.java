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

import fj.Effect;
import fj.F2;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.UnixFsObject;
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
    private final IncludeExcludeFileSelector selector;

    public final Option<F2<UnixFsObject, FileAttributes, FileAttributes>> applyFileAttributes;
    public final Option<F2<UnixFsObject, FileAttributes, FileAttributes>> applyDirectoryAttributes;

    public SetAttributesOperation( RelativePath basedir, List<String> includes, List<String> excludes,
                                   Option<FileAttributes> fileAttributes, Option<FileAttributes> directoryAttributes )
    {
        validateNotNull( basedir, includes, excludes, fileAttributes, directoryAttributes );

        selector = IncludeExcludeFileSelector.build( null ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        if ( fileAttributes.isSome() )
        {
            F2<UnixFsObject, FileAttributes, FileAttributes> f = new ApplyAttributes( UnixFsObject.RegularFile.class, basedir, fileAttributes.some() );
            applyFileAttributes = fromNull( f );
        }
        else
        {
            applyFileAttributes = none();
        }

        if ( directoryAttributes.isSome() )
        {
            F2<UnixFsObject, FileAttributes, FileAttributes> f = new ApplyAttributes( UnixFsObject.Directory.class, basedir, directoryAttributes.some() );
            applyDirectoryAttributes = some( f );
        }
        else
        {
            applyDirectoryAttributes = none();
        }
    }

    public void perform( final FileCollector fileCollector )
        throws IOException
    {
        Effect<F2<UnixFsObject, FileAttributes, FileAttributes>> apply = new Effect<F2<UnixFsObject, FileAttributes, FileAttributes>>()
        {
            public void e( F2<UnixFsObject, FileAttributes, FileAttributes> applyAttributes )
            {
                fileCollector.apply( applyAttributes );
            }
        };

        applyFileAttributes.foreach( apply );
        applyDirectoryAttributes.foreach( apply );
    }

    private final class ApplyAttributes
        implements F2<UnixFsObject, FileAttributes, FileAttributes>
    {
        private final Class<? extends UnixFsObject> klass;
        private final RelativePath basedir;
        private final FileAttributes attributes;

        private ApplyAttributes( Class<? extends UnixFsObject> klass, RelativePath basedir, FileAttributes attributes )
        {
            this.klass = klass;
            this.basedir = basedir;
            this.attributes = attributes;
        }

        public FileAttributes f( UnixFsObject fsObject, FileAttributes currentAttributes )
        {
            if ( !klass.isAssignableFrom( fsObject.getClass() ) )
            {
                return currentAttributes;
            }

            // Remove the basedir part of the path before matching
            String massagedPath;

            if ( basedir == RelativePath.BASE )
            {
                massagedPath = fsObject.path.string;
            }
            else
            {
                if ( !fsObject.path.startsWith( basedir ) )
                {
                    return currentAttributes;
                }

                massagedPath = fsObject.path.string.substring( basedir.string.length() );
            }

            if ( !selector.matches( massagedPath ) )
            {
                return currentAttributes;
            }

            return currentAttributes.useAsDefaultsFor( attributes );
        }
    }
}
