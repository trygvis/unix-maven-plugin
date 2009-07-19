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

import junit.framework.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.util.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncludeExcludeTest
    extends TestCase
{
    public void testBasic()
        throws Exception
    {
        String myProjectPath = new TestUtil( this ).getTestPath( "src/test/resources/my-project" );

        FileSystemManager fsManager = VFS.getManager();
        FileObject myProject = fsManager.resolveFile( myProjectPath );

        assertEquals( FileType.FOLDER, myProject.getType() );

        List<FileObject> selection = new ArrayList<FileObject>();
        myProject.findFiles( IncludeExcludeFileSelector.build( myProject.getName() ).
            addInclude( new PathExpression( "/src/main/unix/files/**" ) ).
            addInclude( new PathExpression( "*.java" ) ).
            addExclude( new PathExpression( "**/huge-file" ) ).
            filesOnly().
//            noDefaultExcludes().
            create(), true, selection );

        System.out.println( "Included:" );
        for ( FileObject fileObject : selection )
        {
            System.out.println( myProject.getName().getRelativeName( fileObject.getName() ) );
        }

        assertEquals( 2, selection.size() );
        assertTrue( selection.contains( myProject.resolveFile( "src/main/unix/files/opt/comp/myapp/etc/myapp.conf" ) ) );
        assertTrue( selection.contains( myProject.resolveFile( "Included.java" ) ) );
    }
}
