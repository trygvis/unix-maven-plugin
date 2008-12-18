package org.codehaus.mojo.unix.maven.pkg;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The logic creating the pkginfo file.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFile
{
    public String packageName;
    public String classifier;
    public PackageVersion version;
    public String name;
    public String description;
    public String arch;
    public String classes = "none";
    public String category = "application";

    public void writeTo( File pkginfo )
        throws IOException, MissingSettingException
    {
        // -----------------------------------------------------------------------
        // Do it!
        // -----------------------------------------------------------------------

        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream( pkginfo );

            Properties properties = new Properties();
            properties.put( "PKG", packageName );
            properties.put( "NAME", StringUtils.clean( name ) );
            properties.put( "DESC", StringUtils.clean( description ) );
            properties.put( "VERSION", getVersion( version ) );
            properties.put( "PSTAMP", getPstamp( version ) );
            properties.put( "CLASSES", getClasses() );
            properties.put( "ARCH", StringUtils.isNotEmpty( arch ) ? arch : "all" );
            properties.put( "CATEGORY", StringUtils.clean( category ) );
            properties.store( stream, null );
        }
        finally
        {
            IOUtil.close( stream );
        }
    }

    public String getPkgName( File pkginfoFile )
        throws IOException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream( pkginfoFile );

            Properties properties = new Properties();
            properties.load( inputStream );

            String packageName = properties.getProperty( "PKG" );

            if ( packageName == null )
            {
                throw new IOException( "Could not read package name (PKG) from pkginfo file: '" +
                    pkginfoFile.getAbsolutePath() + "'." );
            }

            return packageName;
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    public static String getVersion( PackageVersion version )
    {
        return version.getMavenVersion();
    }

    private String getPstamp( PackageVersion version )
    {
        return version.timestamp;
    }

    public String getClasses()
    {
        return StringUtils.isNotEmpty( classes ) ? classes : "none";
    }
}
