package org.codehaus.mojo.unix.rpm;

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
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUtil
{
    public static final SimpleDateFormat DATE_FORMAT_SHORTER = new SimpleDateFormat( "MMM dd HH:mm" );
    public static final SimpleDateFormat DATE_FORMAT_LONGER = new SimpleDateFormat( "MMM dd yyyy" );

    public static final class FileInfo
        implements EqualsIgnoreNull<FileInfo>, LineProducer
    {
        public final String path;

        public final String user;

        public final String group;

        public final String mode;

        public final int size;

        public final Date date;

        public FileInfo( String path, String user, String group, String mode, int size, Date date )
        {
            this.path = path;
            this.user = user;
            this.group = group;
            this.mode = mode;
            this.size = size;
            this.date = date;
        }

        public boolean equalsIgnoreNull( FileInfo that )
        {
            return size == that.size &&
                ( date == null || date.equals( that.date ) ) &&
                group.equals( that.group ) &&
                mode.equals( that.mode ) &&
                path.equals( that.path ) &&
                user.equals( that.user );
        }

        public final boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof FileInfo ) )
            {
                return false;
            }

            FileInfo that = (FileInfo) o;

            return path.equals( that.path );
        }

        public final int hashCode()
        {
            return path.hashCode();
        }

        public void streamTo( LineStreamWriter stream )
        {
            stream.add( new StringBuffer().
                append( mode ).append( " " ).
                append( user ).append( " " ).
                append( group ).append( " " ).
                append( size ).append( " " ).
                append( date != null ? DATE_FORMAT_SHORTER.format( date ) : "<not set>" ).append( " " ).
                append( path ).toString() );
        }
    }

    public static final class SpecFile
        implements EqualsIgnoreNull<SpecFile>, LineProducer
    {
        public String name;
        public String version;
        public int release;
        public String summary;
        public String license;
        public String group;
        public String description;
        public List configFiles;

        public SpecFile( String name, String version, int release, String summary, String license, String group,
                         String description, List configFiles )
        {
            this.name = name;
            this.version = version;
            this.release = release;
            this.summary = summary;
            this.license = license;
            this.group = group;
            this.description = description;
            this.configFiles = configFiles;
        }

        public boolean equalsIgnoreNull( SpecFile that )
        {
            return name.equals( that.name ) &&
                version.equals( that.version ) &&
                release == that.release &&
                summary.equals( that.summary ) &&
                license.equals( that.license ) &&
                group.equals( that.group ) &&
                ( description == null || description.equals( that.description ) );
        }

        public String toString()
        {
            return LineStreamUtil.toString( this );
        }

        public void streamTo( LineStreamWriter stream )
        {
            stream.add( "Name: " + name ).
                add( "Version: " + version ).
                add( "Release: " + release ).
                add( "Summary: " + summary ).
                add( "License: " + license ).
                add( "Group: " + group ).
                add().
                add( "%description " ).
                add( StringUtils.clean( description ) );
        }
    }

    public static List<FileInfo> queryPackageForFileInfo( File rpm )
        throws IOException
    {
        RpmQueryParser parser = new RpmQueryParser();
        new SystemCommand().
            withStdoutConsumer( parser ).
            setCommand( "rpm" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            addArgument( "-q" ).
            addArgument( "-l" ).
            addArgument( "-v" ).
            execute().
            assertSuccess();

        return parser.getList();
    }

    public static SpecFile getSpecFileFromRpm( File rpm )
        throws IOException
    {
        // rpm -q --queryformat "%{NAME}\n%{SIZE}" -p ./unix-maven-plugin/src/it/jetty/target/jetty-1.1-2.rpm

        RpmSpecParser parser = new RpmSpecParser();
        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( parser ).
            withStderrConsumer( System.err ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--queryformat" ).
            addArgument( "%{NAME}\\n%{VERSION}\\n%{RELEASE}\\n%{SUMMARY}\\n%{LICENSE}\\n%{GROUP}\\n" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        ByteArrayOutputStream description = new ByteArrayOutputStream();

        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( description ).
            withStderrConsumer( System.err ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--queryformat" ).
            addArgument( "%{DESCRIPTION}" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        final List<String> configFiles = new LinkedList<String>();

        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( new SystemCommand.StringListLineConsumer( configFiles ) ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--configfiles" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        return new SpecFile( parser.name, parser.version, parser.release, parser.summary, parser.license, parser.group,
            description.toString(), configFiles );
    }

    private static class RpmQueryParser
        implements SystemCommand.LineConsumer
    {
        private List<FileInfo> list = new ArrayList<FileInfo>();

        public void onLine( String line )
            throws IOException
        {
            String[] parts = line.replaceAll( " +", " " ).split( " " );

            // Dunno what the second element is

            Date date;
            try
            {
                date = DATE_FORMAT_SHORTER.parse( parts[5] + " " + parts[6] + " " + parts[7] );
            }
            catch ( ParseException e )
            {
                try
                {
                    date = DATE_FORMAT_LONGER.parse( parts[5] + " " + parts[6] + " " + parts[7] );
                }
                catch ( Exception e2 )
                {
                    e2.printStackTrace();
                    return;
                }
            }

            list.add( new FileInfo( parts[8].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[0].trim(),
                Integer.parseInt( parts[4].trim() ), date ) );
        }

        public List<FileInfo> getList()
        {
            return list;
        }
    }

    private static class RpmSpecParser
        implements SystemCommand.LineConsumer
    {
        public String name;
        public String version;
        public int release;
        public String summary;
        public String license;
        public String group;
        private int count;

        public void onLine( String line )
            throws IOException
        {
            switch ( count++ )
            {
                case 0:
                    name = line;
                    break;
                case 1:
                    version = line;
                    break;
                case 2:
                    release = Integer.parseInt( line );
                    break;
                case 3:
                    summary = line;
                    break;
                case 4:
                    license = line;
                    break;
                case 5:
                    group = line;
                    break;
            }
        }
    }
}
