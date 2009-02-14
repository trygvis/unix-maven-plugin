package org.codehaus.mojo.unix.rpm;

import org.codehaus.mojo.unix.EqualsIgnoreNull;
import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUtil
{
    private static final String EOL = System.getProperty( "line.separator" );
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "MMM dd HH:mm" );

    public static final class FileInfo
        implements EqualsIgnoreNull
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

        public boolean equalsIgnoreNull( EqualsIgnoreNull other )
        {
            FileInfo that = (FileInfo) other;

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

        public String toString()
        {
            return new StringBuffer().
                append( mode ).append( " " ).
                append( user ).append( " " ).
                append( group ).append( " " ).
                append( size ).append( " " ).
                append( date != null ? DATE_FORMAT.format( date ) : "not set" ).append( " " ).
                append( path ).toString();
        }
    }

    public static final class SpecFile
        implements EqualsIgnoreNull
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

        public boolean equalsIgnoreNull( EqualsIgnoreNull other )
        {
            SpecFile that = (SpecFile) other;

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
            return new StringBuffer().
                append( "Name: " ).append( name ).append( EOL ).
                append( "Version: " ).append( version ).append( EOL ).
                append( "Release: " ).append( release ).append( EOL ).
                append( "Summary: " ).append( summary ).append( EOL ).
                append( "License: " ).append( license ).append( EOL ).
                append( "Group: " ).append( group ).append( EOL ).
                append( EOL ).
                append( "%description " ).append( EOL ).
                append( StringUtils.clean( description ) ).toString();
        }
    }

    public static List queryPackageForFileInfo( File rpm )
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
        private List list = new ArrayList();

        public void onLine( String line )
            throws IOException
        {
            String[] parts = line.replaceAll( " +", " " ).split( " " );

            // Dunno what the second element is

            try
            {
                list.add( new FileInfo( parts[8].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[0].trim(),
                    Integer.parseInt( parts[4].trim() ),
                    DATE_FORMAT.parse( parts[5] + " " + parts[6] + " " + parts[7] ) ) );
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
            }
        }

        public List getList()
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
