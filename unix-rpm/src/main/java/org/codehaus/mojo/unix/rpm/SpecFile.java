package org.codehaus.mojo.unix.rpm;

import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.util.line.LineWriterWriter;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.mojo.unix.util.UnixUtil;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFile
{
    public String groupId;

    public String artifactId;

    public PackageVersion version;

    // Will be generated if not set
    public String name;

    public String summary;

    public String license;

    public String distribution;

    public File icon;

    public String vendor;

    public String url;

    public String group;

    public String packager;

    public List defineStatements = new ArrayList();

    public List provides = new ArrayList();

    public List requires = new ArrayList();

    public List conflicts = new ArrayList();

    public String prefix;

    public File buildRoot;

    public String description;

    public boolean dump;

    private LinkedHashSet files = new LinkedHashSet();

    public File includePre;

    public File includePost;

    public File includePreun;

    public File includePostun;

    public void addFile( RelativePath path, FileAttributes attributes )
        throws IOException
    {
        files.add( new StringBuffer().
            append( "%attr(" ).append( attributes.mode != null ? attributes.mode.toOctalString() : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( attributes.user ) ? attributes.user : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( attributes.group ) ? attributes.group : "-" ).append( ") " ).
            append( path.asAbsolutePath() ).toString() );
    }

    public void addDirectory( RelativePath path, FileAttributes attributes )
    {
        files.add( new StringBuffer().
            append( "%dir " ).
            append( "%attr(" ).append( attributes.mode != null ? attributes.mode.toOctalString() : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( attributes.user ) ? attributes.user : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( attributes.group ) ? attributes.group : "-" ).append( ") " ).
            append( path.asAbsolutePath() ).toString() );
    }

    public void writeToFile( File specFile )
        throws IOException, MissingSettingException
    {
        PrintWriter spec = null;

        try
        {
            spec = new PrintWriter( new FileWriter( specFile ) );
            writeTo( spec );
        }
        finally
        {
            IOUtil.close( spec );
        }
    }

    public void writeTo( PrintWriter writer )
        throws MissingSettingException, IOException
    {
        LineWriterWriter spec = new LineWriterWriter( writer );

        for ( Iterator it = defineStatements.iterator(); it.hasNext(); )
        {
            spec.add( "%define " + it.next() );
        }

        UnixUtil.assertField( "version", version );

        spec.
            add( "Name: " + UnixUtil.getField( "name", getName() ) ).
            add( "Version: " + getRpmVersion( version ) ).
            add( "Release: " + getRpmRelease( version ) ).
            add( "Summary: " + UnixUtil.getField( "summary", summary ) ).
            add( "License: " + UnixUtil.getField( "license", license ) ).
            addIfNotEmpty( "Distribution: ", distribution ).
//            addIf(icon != null, "Icon").addIfNotNull( icon ).
//            addIfNotEmpty( "Vendor", vendor ).
//            addIfNotEmpty( "URL", url ).
            add( "Group: " + UnixUtil.getField( "group", group ) ).
            addIfNotEmpty( "Packager", packager ).
            setPrefix( "Provides" ).addAllLines( provides ).clearPrefix().
            setPrefix( "Requires" ).addAllLines( requires ).clearPrefix().
            setPrefix( "Conflicts" ).addAllLines( conflicts ).clearPrefix().
            add( "BuildRoot: " + UnixUtil.getField( "buildRoot", buildRoot ).getAbsolutePath() ).
            add();

        // The %description tag is required even if it is empty.
        spec.
            add( "%description" ).
            addIf( StringUtils.isNotEmpty( description ), description ).
            add();

        spec.
            add( "%files" ).
            addAllLines( files );

        spec.addIf( includePre != null || includePost != null || includePreun != null || includePostun != null, "" );
        if ( includePre != null )
        {
            spec.add( "%pre" );
            spec.add( "%include " + includePre.getAbsolutePath() );
        }
        if ( includePost != null )
        {
            spec.add( "%post" );
            spec.add( "%include " + includePost.getAbsolutePath() );
        }
        if ( includePreun != null )
        {
            spec.add( "%preun" );
            spec.add( "%include " + includePreun.getAbsolutePath() );
        }
        if ( includePostun != null )
        {
            spec.add( "%postun" );
            spec.add( "%include " + includePostun.getAbsolutePath() );
        }

        spec.addIf( dump, "%dump" );
    }

    private String getName()
    {
        if ( name != null )
        {
            return name;
        }

        if ( StringUtils.isEmpty( groupId ) || StringUtils.isEmpty( artifactId ) )
        {
            throw new RuntimeException( "Both group id and artifact id has to be set." );
        }

        String name = groupId + "-" + artifactId;

        name = name.toLowerCase();

        return name;
    }

    private static String getRpmVersion( PackageVersion version )
    {
        String rpmVersionString = version.version;

        if ( version.snapshot )
        {
            rpmVersionString += "_" + version.timestamp;
        }

        return rpmVersionString.replace( '-', '_' );
    }

    private static int getRpmRelease( PackageVersion version )
    {
        return version.revision;
    }
}
