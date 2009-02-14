package org.codehaus.mojo.unix.util.vfs;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    private FileType fileType;
    private List<PathExpression> includes;
    private List<PathExpression> excludes;

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
