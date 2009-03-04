package org.codehaus.mojo.unix.core;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributesOperationTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
/*
        FileObject basefile = getBaseFileObject();

        FileObject from = basefile.resolveFile( "src/test/resources/operation/files" );

        assertEquals( FileType.FOLDER, from.getType() );
        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        RelativePath opt = fromString( "opt" );
        RelativePath jetty = opt.add( "jetty" );
        RelativePath bash_profile = jetty.add( ".bash_profile" );
        RelativePath readmeUnix = jetty.add( "README-unix.txt" );
        RelativePath bin = jetty.add( "bin" );
        RelativePath extraApp = bin.add( "extra-app" );

        control.expectAndReturn( fileCollector.setAttributes( extraApp, fileAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( bash_profile, fileAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( readmeUnix, fileAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( bin, directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( jetty, directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( opt, directoryAttributes ), fileCollector );
        control.expectAndReturn( fileCollector.setAttributes( RelativePath.BASE, directoryAttributes ), fileCollector );
        control.replay();

        new SetAttributesOperation( null, from, RelativePath.BASE, null, null, fileAttributes, directoryAttributes ).
            perform( fileCollector );

        control.verify();
*/
    }
}
