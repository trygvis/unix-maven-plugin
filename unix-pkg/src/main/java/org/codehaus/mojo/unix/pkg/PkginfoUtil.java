package org.codehaus.mojo.unix.pkg;

import org.codehaus.mojo.unix.EqualsIgnoreNull;
import org.codehaus.mojo.unix.util.SystemCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoUtil
{
    private static final String EOL = System.getProperty( "line.separator" );

    public static class PackageInfo
        implements EqualsIgnoreNull
    {
        public final String instance;

        public final String name;

        public final String category;

        public final String arch;

        public final String version;

        public final String desc;

        public final String pstamp;

        public PackageInfo( String instance, String name, String category, String arch, String version, String desc,
                            String pstamp )
        {
            this.instance = instance;
            this.name = name;
            this.category = category;
            this.arch = arch;
            this.version = version;
            this.desc = desc;
            this.pstamp = pstamp;
        }

        public String toString()
        {
            return new StringBuffer().
                append( " PKGINST: " ).append( instance ).append( EOL ).
                append( "    NAME: " ).append( name ).append( EOL ).
                append( "    ARCH: " ).append( arch ).append( EOL ).
                append( " VERSION: " ).append( version ).append( EOL ).
                append( "CATEGORY: " ).append( category ).append( EOL ).
                append( "    DESC: " ).append( desc != null ? desc : "" ).append( EOL ).
                append( "  PSTAMP: " ).append( pstamp != null ? pstamp : "").append( EOL ).toString();
        }

        public boolean equalsIgnoreNull( EqualsIgnoreNull other )
        {
            PackageInfo that = (PackageInfo) other;

            return instance.equals( that.instance ) &&
                name.equals( that.name ) &&
                category.equals( that.category ) &&
                arch.equals( that.arch ) &&
                version.equals( that.version ) &&
                ( desc == null || desc.equals( that.desc ) ) &&
                ( pstamp == null || pstamp.equals( that.pstamp ) );
        }
    }

    public static PackageInfo getPackageInforForDevice( File device )
        throws IOException
    {
        return getPackageInforForDevice( device, null );
    }

    public static PackageInfo getPackageInforForDevice( File device, String instance )
        throws IOException
    {
        if ( !device.canRead() )
        {
            throw new FileNotFoundException( device.getAbsolutePath() );
        }

        PkginfoParser parser = new PkginfoParser();
        new SystemCommand().
            dumpCommandIf( true ).
            withStderrConsumer( parser ).
            withStdoutConsumer( parser ).
            setCommand( "pkginfo" ).
            addArgument( "-d" ).
            addArgument( device.getAbsolutePath() ).
            addArgument( "-l" ).
            addArgumentIfNotEmpty( instance ).
            execute().
            assertSuccess();

        // ( StringUtils.isNotEmpty( instance ) ? this : addArgument(instance) )

        return parser.getPackageInfo();
    }

    private static class PkginfoParser
        implements SystemCommand.LineConsumer
    {
        private String instance;

        private String name;

        private String category;

        private String arch;

        private String version;

        private String desc;

        private String pstamp;

        public void onLine( String line )
        {
            int i = line.indexOf( ':' );

            if ( i == -1 )
            {
                return;
            }

            String field = line.substring( 0, i ).trim();
            String value = line.substring( i + 1 ).trim();

            if ( "PKGINST".equals( field ) )
            {
                instance = value;
            }
            else if ( "NAME".equals( field ) )
            {
                name = value;
            }
            else if ( "CATEGORY".equals( field ) )
            {
                category = value;
            }
            else if ( "ARCH".equals( field ) )
            {
                arch = value;
            }
            else if ( "VERSION".equals( field ) )
            {
                version = value;
            }
            else if ( "DESC".equals( field ) )
            {
                desc = value;
            }
            else if ( "PSTAMP".equals( field ) )
            {
                pstamp = value;
            }
        }

        public PackageInfo getPackageInfo()
        {
            return new PackageInfo( instance, name, category, arch, version, desc, pstamp );
        }
    }
}
