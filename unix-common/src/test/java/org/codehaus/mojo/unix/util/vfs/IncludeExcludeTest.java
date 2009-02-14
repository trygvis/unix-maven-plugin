package org.codehaus.mojo.unix.util.vfs;

import junit.framework.TestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.codehaus.plexus.PlexusTestCase;

import java.util.ArrayList;
import java.util.List;

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
        String myProjectPath = PlexusTestCase.getTestPath( "src/test/resources/my-project" );

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
            System.out.println(myProject.getName().getRelativeName(fileObject.getName()));
        }

        assertEquals( 2, selection.size() );
        assertTrue( selection.contains( myProject.resolveFile( "src/main/unix/files/opt/comp/myapp/etc/myapp.conf" ) ) );
        assertTrue( selection.contains( myProject.resolveFile( "Included.java" ) ) );
    }
}
