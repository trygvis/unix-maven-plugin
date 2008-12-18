package org.codehaus.mojo.unix.dpkg;

import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.mojo.unix.HasRelaxedEquality;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgDebUtil
{
    private static final String EOL = System.getProperty( "line.separator" );

    public static class ControlFile
        implements HasRelaxedEquality
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

        public boolean equalsIgnoreNull( HasRelaxedEquality other )
        {
            ControlFile that = (ControlFile) other;

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
