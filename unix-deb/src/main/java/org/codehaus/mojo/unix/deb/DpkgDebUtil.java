package org.codehaus.mojo.unix.deb;

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

import static fj.data.List.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgDebUtil
{
    public static ControlFile getControlFile( File deb )
        throws IOException
    {
        SystemCommand.StringListLineConsumer list = new SystemCommand.StringListLineConsumer();
        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( list ).
            withStderrConsumer( System.err ).
            setCommand( "dpkg-deb" ).
            addArgument( "-f" ).
            addArgument( deb.getAbsolutePath() ).
            addArgument( "section" ).
            addArgument( "priority" ).
            addArgument( "maintainer" ).
            addArgument( "package" ).
            addArgument( "version" ).
            addArgument( "architecture" ).
            execute().
            assertSuccess();

        ByteArrayOutputStream description = new ByteArrayOutputStream();

        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( description ).
            withStderrConsumer( System.err ).
            setCommand( "dpkg-deb" ).
            addArgument( "-f" ).
            addArgument( deb.getAbsolutePath() ).
            addArgument( "description" ).
            execute().
            assertSuccess();

        DebControlParser parser = new DebControlParser();

        ControlFile controlFile = parser.parse( iterableList( list.strings ) );

        controlFile.description( some( description.toString().trim() ) );

        return controlFile;
    }
}
