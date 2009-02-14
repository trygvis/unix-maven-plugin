package org.codehaus.mojo.unix.util.vfs;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PathExpressionTest
    extends TestCase
{
    public void testBasic()
    {
        String[] patterns = new String[]{
            "^.*/[^/]*~$",                  // **/*~
            "^.*/#[^/]*#$",                 // **/#*#
            "^.*/\\.#[^/]*$",               // **/.#*
            "^.*/%[^/]*%$",                 // **/%*%
            "^.*/\\._[^/]*$",               // **/._*
            "^.*/CVS$",                     // **/CVS
            "^.*/CVS/.*$",                  // **/CVS/**
            "^.*/\\.cvsignore$",            // **/.cvsignore
            "^.*/SCCS$",                    // **/SCCS
            "^.*/SCCS/.*$",                 // **/SCCS/**
            "^.*/vssver\\.scc$",            // **/vssver.scc
            "^.*/\\.svn$",                  // **/.svn
            "^.*/\\.svn/.*$",               // **/.svn/**
            "^.*/\\.DS_Store$",             // **/.DS_Store
            "^.*/META-INF$",                // **/META-INF
            "^.*/META-INF/.*$"              // **/META-INF/**
        };

        assertEquals( IncludeExcludeFileSelector.DEFAULT_EXCLUDES.length, patterns.length );

        for ( int i = 0; i < IncludeExcludeFileSelector.DEFAULT_EXCLUDES.length; i++ )
        {
            String pattern = IncludeExcludeFileSelector.DEFAULT_EXCLUDES[i].getPattern().pattern();
            if(!pattern.equals( patterns[i] ))
            {
                System.out.println( i + ": ok=" + pattern.equals( patterns[i] ) + ", expression = " + IncludeExcludeFileSelector.DEFAULT_EXCLUDES[i].getExpression() + ", expected = " + patterns[i] + ", actual = " + pattern );
            }
            assertEquals( patterns[i], IncludeExcludeFileSelector.DEFAULT_EXCLUDES[i].getPattern().pattern() );
        }

        assertEquals( "^/[^/]*\\.java$", new PathExpression( "*.java" ).getPattern().pattern() );
        assertEquals( "^/copyright$", new PathExpression("copyright").getPattern().pattern() );
    }
}
