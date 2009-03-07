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
