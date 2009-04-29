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

import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncludeExcludeFileSelector
    implements FileSelector
{
    public final static PathExpression[] DEFAULT_EXCLUDES = new PathExpression[]{
        new PathExpression( "**/*~" ),
        new PathExpression( "**/#*#" ),
        new PathExpression( "**/.#*" ),
        new PathExpression( "**/%*%" ),
        new PathExpression( "**/._*" ),
        new PathExpression( "**/CVS" ),
        new PathExpression( "**/CVS/**" ),
        new PathExpression( "**/.cvsignore" ),
        new PathExpression( "**/SCCS" ),
        new PathExpression( "**/SCCS/**" ),
        new PathExpression( "**/vssver.scc" ),
        new PathExpression( "**/.svn" ),
        new PathExpression( "**/.svn/**" ),
        new PathExpression( "**/.DS_Store" ),
        new PathExpression( "**/META-INF" ),
        new PathExpression( "**/META-INF/**" )
    };

    private final FileName root;
    private final FileType fileType;
    private final List<PathExpression> includes;
    private final List<PathExpression> excludes;

    IncludeExcludeFileSelector( FileName root, FileType fileType, List<PathExpression> includes, List<PathExpression> excludes )
    {
        this.root = root;
        this.fileType = fileType;
        this.includes = includes;
        this.excludes = excludes;
    }

    public boolean includeFile( FileSelectInfo fileSelectInfo )
        throws Exception
    {
        FileObject fileObject = fileSelectInfo.getFile();
        FileName name = fileObject.getName();

        if ( fileType != null && fileObject.getType() != fileType )
        {
            return false;
        }

        String relativePath = root.getRelativeName( name );

        return matches( relativePath );
    }

    public boolean matches( String relativePath )
    {
        // -----------------------------------------------------------------------
        // Make sure that the relative path always starts with a leading slash as
        // people want to match against "**/.svn**" which would not match the
        // root .svn directory.
        // -----------------------------------------------------------------------

        relativePath = "/" + relativePath;

        boolean include;

        if( includes.size() == 0 )
        {
            // Default to including the file is the list of includes is empty
            include = true;

            include = matchesAny( include, false, relativePath, excludes );
        }
        else
        {
            // Default to not including the file if the list of includes is non-empty
            include = false;

            include = matchesAny( include, true, relativePath, includes );

            include = matchesAny( include, false, relativePath, excludes );
        }

//        if ( !include )
//        {
//            System.out.println( "No match: " + relativePath );
//        }

        return include;
    }

    public boolean traverseDescendents( FileSelectInfo fileSelectInfo )
        throws Exception
    {
        return true;
    }

    private boolean matchesAny( boolean include, boolean returnIfMatch, String relative, Collection<PathExpression> pathExpressions )
    {
        for (PathExpression pathExpression : pathExpressions )
        {
            if (pathExpression.matches(relative)) {
                return returnIfMatch;
            }
        }

        return include;
    }

    public static class TemplateIncludeExcludeFileSelector
    {
        private FileName name;

        private List<PathExpression> includes = new ArrayList<PathExpression>( 100 );
        private List<PathExpression> excludes = new ArrayList<PathExpression>( 100 );
        private FileType fileType;
        private boolean addDefaultExcludes = true;

        private TemplateIncludeExcludeFileSelector( FileName name )
        {
            this.name = name;
        }

        public TemplateIncludeExcludeFileSelector noDefaultExcludes()
        {
            addDefaultExcludes = false;

            return this;
        }

        public TemplateIncludeExcludeFileSelector addInclude( PathExpression include )
        {
            includes.add( include );

            return this;
        }

        public TemplateIncludeExcludeFileSelector addStringIncludes( List<String> includes )
        {
            if ( includes == null )
            {
                return this;
            }

            for (String include : includes)
            {
                this.includes.add( new PathExpression( include ) );
            }

            return this;
        }

        public TemplateIncludeExcludeFileSelector addExclude( PathExpression exclude )
        {
            excludes.add( exclude );

            return this;
        }

        public TemplateIncludeExcludeFileSelector addStringExcludes( List<String> excludes )
        {
            if ( excludes == null )
            {
                return this;
            }

            for ( String exclude : excludes )
            {
                this.excludes.add( new PathExpression( exclude ) );
            }

            return this;
        }

        public IncludeExcludeFileSelector create()
        {
            if ( addDefaultExcludes )
            {
                excludes.addAll( Arrays.asList( DEFAULT_EXCLUDES ) );
            }

//            System.out.println( "Includes:" );
//            for (PathExpression pathExpression : includes)
//            {
//                System.out.println(pathExpression.getExpression() + "=" + pathExpression.getPattern().pattern());
//            }
//
//            System.out.println( "Excludes:" );
//            for ( PathExpression pathExpression : excludes )
//            {
//                System.out.println(pathExpression.getExpression() + "=" + pathExpression.getPattern().pattern());
//            }

            return new IncludeExcludeFileSelector( name, fileType, includes, excludes );
        }

        public TemplateIncludeExcludeFileSelector filesOnly()
        {
            fileType = FileType.FILE;

            return this;
        }
    }

    public static TemplateIncludeExcludeFileSelector build( FileName name )
    {
        return new TemplateIncludeExcludeFileSelector( name );
    }
}
