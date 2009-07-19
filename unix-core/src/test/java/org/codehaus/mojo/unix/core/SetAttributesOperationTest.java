package org.codehaus.mojo.unix.core;

import fj.data.*;
import static fj.data.Option.*;
import fj.*;
import junit.framework.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.UnixFileMode.*;
import static org.codehaus.mojo.unix.core.OperationTest.*;
import org.codehaus.mojo.unix.util.*;
import org.easymock.*;
import org.easymock.internal.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributesOperationTest
    extends TestCase
{
    public final static FileAttributes fileAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( _0755 ) );

    public final static FileAttributes directoryAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0644 ) );

    public void testBasic()
        throws Exception
    {
        Option<FileAttributes> changedAttributes = some( EMPTY.user( "myuser" ) );

        assertEquals( FileType.FOLDER, files.files.getType() );
        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        fileCollector.addFile( files.optJettyBinExtraApp, objects.optJettyBinExtraApp );
        control.setMatcher( new OperationTest.FileObjectMatcher() );
        control.setReturnValue( fileCollector );

        fileCollector.addFile( files.optJettyReadmeUnix, objects.optJettyReadmeUnix );
        control.setReturnValue( fileCollector );

        fileCollector.addFile( files.optJettyBashProfile, objects.optJettyBashProfile );
        control.setReturnValue( fileCollector );

        control.expectAndReturn( fileCollector.addDirectory( objects.optJettyBin ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.optJetty ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.opt ), fileCollector );
        control.expectAndReturn( fileCollector.addDirectory( objects.base ), fileCollector );
        fileCollector.apply( null );
        control.setMatcher( new AlwaysMatcher() );
        control.replay();

        new CopyDirectoryOperation( files.files, RelativePath.BASE, null, null, Option.<P2<String, String>>none(),
                                    fileAttributes, directoryAttributes ).perform( fileCollector );

        new SetAttributesOperation( RelativePath.BASE, Collections.<String>emptyList(), Collections.<String>emptyList(),
            Option.<FileAttributes>none(), Option.<FileAttributes>none() ).perform( fileCollector );

        new SetAttributesOperation( RelativePath.BASE, Collections.singletonList( "**/bin/extra-app" ), Collections.<String>emptyList(),
            changedAttributes, Option.<FileAttributes>none() ).perform( fileCollector );

        control.verify();
    }

    public void testApplyAttributes()
    {
        FileAttributes defaultAttributes = FileAttributes.EMPTY.user( "default" ).group( "default" ).mode( _0755 );

        SetAttributesOperation operation = new SetAttributesOperation( RelativePath.BASE,
            Collections.singletonList( "**/bin/*" ), Collections.<String>emptyList(),
            some( EMPTY.user( "myuser" )), some( EMPTY ) );

        assertEquals( defaultAttributes, operation.applyFileAttributes.some().f( objects.optJettyReadmeUnix, defaultAttributes ) );

        assertEquals( defaultAttributes, operation.applyDirectoryAttributes.some().f( objects.optJettyBin, defaultAttributes ) );
        assertEquals( defaultAttributes.user( "myuser" ), operation.applyFileAttributes.some().f( objects.optJettyBinExtraApp, defaultAttributes ) );
    }
}
