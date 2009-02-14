package org.codehaus.mojo.unix.pkg;

import org.codehaus.mojo.unix.util.SystemCommand;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgchkUtil
{
    private static final String EOL = System.getProperty( "line.separator" );
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "MMM dd HH:mm:ss yyyy" );

    public static abstract class FileInfo
    {
        public final String pathname;

        public final String type;

        public final int fileSize;

        public final int sum;

        public final Date lastModification;

        public FileInfo( String pathname, String type, int fileSize, int sum,
                         Date lastModification )
        {
            this.pathname = pathname;
            this.type = type;
            this.fileSize = fileSize;
            this.sum = sum;
            this.lastModification = lastModification;
        }

        public boolean equalsIgnoreNull( FileInfo that )
        {
            return pathname.equals( that.pathname ) &&
                ( type == null || type.equals( that.type ) ) &&
                ( fileSize == 0 || fileSize == that.fileSize ) &&
                ( sum == 0 || sum == that.sum ) &&
                ( lastModification == null || lastModification.equals( that.lastModification ) );
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

            return pathname.equals( that.pathname );
        }

        public final int hashCode()
        {
            return pathname.hashCode();
        }

        public String toString()
        {
            return new StringBuffer().
                append( "Pathname: " ).append( pathname ).append( EOL ).
                append( "Type: " ).append( type ).append( EOL ).
                append( "Expected file size (bytes): " ).append( fileSize ).append( EOL ).
                append( "Expected sum(1) of contents: " ).append( sum ).append( EOL ).
                append( "Expected last modification: " ).append( lastModification != null ? DATE_FORMAT.format( lastModification ) : "not set" ).append( EOL ).
                toString();
        }
    }

    public static class InstallationFile
        extends FileInfo
    {
        public InstallationFile( String pathname, int fileSize, int sum, Date lastModification )
        {
            super( pathname, "installation file", fileSize, sum, lastModification );
        }
    }

    private abstract static class AbstractFile
        extends FileInfo
    {
        public final String mode;

        public final String owner;

        public final String group;

        public AbstractFile( String pathname, String type, String mode, String owner, String group, int fileSize,
                             int sum, Date lastModification )
        {
            super( pathname, type, fileSize, sum, lastModification );

            this.mode = mode;
            this.owner = owner;
            this.group = group;
        }

        public boolean equalsIgnoreNull( FileInfo t )
        {
            AbstractFile that = (AbstractFile) t;
            return super.equalsIgnoreNull( that ) &&
                mode.equals( that.mode ) &&
                owner.equals( that.owner ) &&
                group.equals( that.group );
        }

        public String toString()
        {
            return new StringBuffer().
                append( "Pathname: " ).append( pathname ).append( EOL ).
                append( "Type: " ).append( type ).append( EOL ).
                append( "Expected mode: " ).append( mode ).append( EOL ).
                append( "Expected owner: " ).append( owner ).append( EOL ).
                append( "Expected group: " ).append( group ).append( EOL ).
                append( "Expected file size (bytes): " ).append( fileSize ).append( EOL ).
                append( "Expected sum(1) of contents: " ).append( sum ).append( EOL ).
                append( "Expected last modification: " ).append( lastModification != null ? DATE_FORMAT.format( lastModification ) : "not set" ).append( EOL ).
                toString();
        }
    }

    public static class Directory
        extends AbstractFile
    {
        public Directory( String pathname, String mode, String owner, String group, Date lastModification )
        {
            super( pathname, "directory", mode, owner, group, 0, 0, lastModification );
        }
    }

    public static class RegularFile
        extends AbstractFile
    {
        public RegularFile( String pathname, String mode, String owner, String group, int fileSize, int sum,
                            Date lastModification )
        {
            super( pathname, "regular file", mode, owner, group, fileSize, sum, lastModification );
        }
    }

    public static List getPackageInforForDevice( File device )
        throws IOException
    {
        return getPackageInforForDevice( device, "all" );
    }

    public static List getPackageInforForDevice( File device, String instance )
        throws IOException
    {
        PkgchkParser parser = new PkgchkParser();
        new SystemCommand().
            withStdoutConsumer( parser ).
            setCommand( "/usr/sbin/pkgchk" ).
            addArgument( "-l" ).
            addArgument( "-d" ).
            addArgument( device.getAbsolutePath() ).
            addArgument( instance ).
            execute().
            assertSuccess();

        return parser.getList();
    }

    public static boolean available()
    {
        return SystemCommand.available( "pkgchk" );
    }

    private static class PkgchkParser
        implements SystemCommand.LineConsumer
    {
        public String pathname;

        public String type;

        public String mode;

        public String owner;

        public String group;

        public int fileSize;

        public int sum;

        public Date lastModification;

        private List list = new ArrayList();

        public void onLine( String line )
            throws IOException
        {
            if ( line.startsWith( "Pathname: " ) )
            {
                pathname = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Type: " ) )
            {
                type = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected mode: " ) )
            {
                mode = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected owner: " ) )
            {
                owner = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected group: " ) )
            {
                group = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected file size (bytes): " ) )
            {
                fileSize = Integer.parseInt( line.substring( line.indexOf( ':' ) + 1 ).trim() );
            }
            else if ( line.startsWith( "Expected sum(1) of contents: " ) )
            {
                sum = Integer.parseInt( line.substring( line.indexOf( ':' ) + 1 ).trim() );
            }
            else if ( line.startsWith( "Expected last modification: " ) )
            {
                line = line.substring( line.indexOf( ':' ) + 1 ).trim();
                try
                {
                    lastModification = DATE_FORMAT.parse( line );
                }
                catch ( ParseException e )
                {
                    throw new IOException( "Unable to parse last modification: '" + line + "'." );
                }
            }
            else if ( line.trim().length() == 0 )
            {
                if ( type.equals( "regular file" ) )
                {
                    list.add( new RegularFile( pathname, mode, owner, group, fileSize, sum, lastModification ) );
                }
                else if ( type.equals( "installation file" ) )
                {
                    list.add( new InstallationFile( pathname, fileSize, sum, lastModification ) );
                }
                else if ( type.equals( "directory" ) )
                {
                    list.add( new Directory( pathname, mode, owner, group, lastModification ) );
                }
            }
        }

        public List getList()
        {
            return list;
        }
    }
}
