package org.codehaus.mojo.unix.maven.plugin;

/*
 * The MIT License
 *
 * Copyright 2012 The Codehaus.
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

import fj.data.List;
import static fj.data.List.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;

import java.util.*;
import java.util.regex.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@SuppressWarnings( "UnusedDeclaration" )
public class FilterFiles
    extends AssemblyOp
{
    private List<String> includes = nil();

    private List<String> excludes = nil();

    public FilterFiles()
    {
        super( "filter-files" );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setIncludes( String[] includes )
    {
        this.includes = list( includes );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setExcludes( String[] excludes )
    {
        this.excludes = list( excludes );
    }

    public FileFilterDescriptor toDescriptor( Map<String, String> properties )
    {
        List<UnixFsObject.Filter> filter = nil();

        for ( Map.Entry<String, String> entry : properties.entrySet() )
        {
            // This needs to be quoted, patterns like "${project.version}" are not very useful regular expressions.
            Pattern pattern = Pattern.compile( Pattern.quote( entry.getKey() ) );

            filter = filter.cons( new UnixFsObject.Filter( pattern, entry.getValue() ) );
        }

        return new FileFilterDescriptor( includes, excludes, filter );
    }
}
