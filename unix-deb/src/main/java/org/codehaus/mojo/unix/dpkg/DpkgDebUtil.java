package org.codehaus.mojo.unix.dpkg;

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

import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.plexus.util.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgDebUtil
{
    private static final String EOL = System.getProperty( "line.separator" );

    public static class ControlFile
        implements EqualsIgnoreNull<ControlFile>
    {
        public final String section;
        public final String priority;
        public final String maintainer;
        public final String packageName;
        public final String version;
        public final String architecture;
        public final String description;

        public ControlFile( String section, String priority, String maintainer, String packageName, String version,
                            String architecture, String description )
        {
            this.section = section;
            this.priority = priority;
            this.maintainer = maintainer;
            this.packageName = packageName;
            this.version = version;
            this.architecture = architecture;
            this.description = description;
        }

        public boolean equalsIgnoreNull( ControlFile that )
        {
            return section.equals( that.section ) &&
                priority.equals( that.priority ) &&
                maintainer.equals( that.maintainer ) &&
                packageName.equals( that.packageName ) &&
                version.equals( that.version ) &&
                architecture.equals( that.architecture ) &&
                ( description == null || description.equals( that.description ) );
        }

        public String toString()
        {
            return new StringBuffer().
                append( "Section: " ).append( section ).append( EOL ).
                append( "Priority: " ).append( priority ).append( EOL ).
                append( "Maintainer: " ).append( maintainer ).append( EOL ).
                append( "Package: " ).append( packageName ).append( EOL ).
                append( "Version: " ).append( version ).append( EOL ).
                append( "Architecture: " ).append( architecture ).append( EOL ).
                append( "Description: " ).append( StringUtils.clean( description ) ).append( EOL ).
                toString();
        }
    }

    public static ControlFile getControlFile( File deb )
        throws IOException
    {
        // rpm -q --queryformat "%{NAME}\n%{SIZE}" -p ./unix-maven-plugin/src/it/jetty/target/jetty-1.1-2.rpm

        DebControlParser parser = new DebControlParser();
        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( parser ).
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

        return new ControlFile( parser.section, parser.priority, parser.maintainer, parser.packageName, parser.version,
            parser.architecture, description.toString().trim() );
    }

    private static class DebControlParser
        implements SystemCommand.LineConsumer
    {
        public String section;
        public String priority;
        public String maintainer;
        public String packageName;
        public String version;
        public String architecture;

        private int count;

        public void onLine( String line )
            throws IOException
        {
            switch ( count++ )
            {
                case 0:
                    section = line.split( ":" )[1].trim();
                    break;
                case 1:
                    priority = line.split( ":" )[1].trim();
                    break;
                case 2:
                    maintainer = line.split( ":" )[1].trim();
                    break;
                case 3:
                    packageName = line.split( ":" )[1].trim();
                    break;
                case 4:
                    version = line.split( ":" )[1].trim();
                    break;
                case 5:
                    architecture = line.split( ":" )[1].trim();
                    break;
            }
        }
    }
}
