package org.codehaus.mojo.unix.maven.dpkg;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.util.UnixUtil;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class ControlFile
{
    private static final String EOL = System.getProperty( "line.separator" );

    public String groupId;

    public String artifactId;

    public PackageVersion version;

    public Set dependencies;

    // Generic

    public String description;

    public String shortDescription;

    public String maintainer;

    // Debian specific
    public String _package;

    public String architecture = "any";

    public String priority;

    public String section;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public String getDepends()
    {
        if ( dependencies == null || dependencies.size() <= 0 )
        {
            return null;
        }

        String depends = "";

        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        {
            DebianDependency debianDependency = (DebianDependency) it.next();

            if ( depends.length() > 0 )
            {
                depends += ", ";
            }

            // This will happen if this is an extra dependency
            if ( StringUtils.isNotEmpty( debianDependency.getGroupId() ) )
            {
                depends += debianDependency.getGroupId() + "-";
            }

            depends += debianDependency.getArtifactId() + " ";

            // This will happen if this is an extra dependency
            if ( StringUtils.isNotEmpty( debianDependency.getVersion() ) )
            {
                depends += "(" + debianDependency.getVersion() + ")";
            }
        }

        return depends;
    }

    public String getPackage()
    {
        if ( _package != null )
        {
            return _package;
        }

        if ( StringUtils.isEmpty( groupId ) || StringUtils.isEmpty( artifactId ) )
        {
            throw new RuntimeException( "Both group id and artifact id has to be set." );
        }

        String name = groupId + "-" + artifactId;

        name = name.toLowerCase();

        return name;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void toFile( File basedir )
        throws IOException, MissingSettingException
    {
        File debian = new File( basedir, "DEBIAN" );

        if ( !debian.exists() && !debian.mkdirs() )
        {
            throw new IOException( "Could not make directory: " + debian.getAbsolutePath() );
        }

        File control = new File( debian, "control" );

        StringWriter string = new StringWriter();
        PrintWriter output = new PrintWriter( string );

        output.println( "Section: " + UnixUtil.getField( "section", section ) );
        output.println( "Priority: " + UnixUtil.getFieldOrDefault( priority, "standard" ) );
        output.println( "Maintainer: " + UnixUtil.getField( "maintainer", maintainer ) );
        output.println( "Package: " + getPackage() );
        output.println( "Version: " + getDebianVersion( version ) );
        output.println( "Architecture: " + UnixUtil.getField( "architecture", architecture ) );
        String depends = getDepends();
        if ( depends != null )
        {
            output.println( "Depends: " + depends );
        }
        output.println( "Description: " + getDebianDescription() );

        FileUtils.fileWrite( control.getAbsolutePath(), string.toString() );
    }

    private String getDebianVersion( PackageVersion version )
    {
        String v = version.version;

        if ( version.revision > 0 )
        {
            v += "-" + version.revision;
        }

        if ( !version.snapshot )
        {
            return v;
        }

        return v + "-" + version.timestamp;
    }

    public String getDebianDescription()
    {
        // ----------------------------------------------------------------------
        // If the short description is set, use it. If not, synthesize one.
        // ----------------------------------------------------------------------

        String sd = StringUtils.clean( shortDescription );

        String d = StringUtils.clean( description );

        if ( sd.length() == 0 )
        {
            int index = d.indexOf( '.' );

            if ( index > 0 )
            {
                sd = d.substring( 0, index + 1 );

                d = d.substring( index + 1 );
            }
        }

        sd = sd.trim();
        d = d.trim();

        if ( d.length() > 0 )
        {
            d = sd + EOL + d;
        }
        else
        {
            d = sd;
        }

        // ----------------------------------------------------------------------
        // Trim each line, replace blank lines with " ."
        // ----------------------------------------------------------------------

        String debianDescription;

        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( d.trim() ) );

            String line;

            debianDescription = reader.readLine();

            line = reader.readLine();

            if ( line != null )
            {
                debianDescription += EOL + " " + line.trim();

                line = reader.readLine();
            }

            while ( line != null )
            {
                line = line.trim();

                if ( line.equals( "" ) )
                {
                    debianDescription += EOL + ".";
                }
                else
                {
                    debianDescription += EOL + " " + line;
                }

                line = reader.readLine();
            }
        }
        catch ( IOException e )
        {
            // This won't happen.
            throw new RuntimeException( "Internal error", e );
        }

        return debianDescription;
    }
}
