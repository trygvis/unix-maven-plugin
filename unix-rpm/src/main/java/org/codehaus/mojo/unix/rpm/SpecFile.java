package org.codehaus.mojo.unix.rpm;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.PackageVersion;
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
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
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

    public void addFile( String path, String user, String group, String mode )
        throws IOException
    {
        files.add( new StringBuffer().
            append( "%attr(" ).append( StringUtils.isNotEmpty( mode ) ? mode : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( user ) ? user : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( group ) ? group : "-" ).append( ") " ).
            append( path ).toString() );
    }

    public void addDirectory( String path, String user, String group, String mode )
    {
        files.add( new StringBuffer().
            append( "%dir " ).
            append( "%attr(" ).append( StringUtils.isNotEmpty( mode ) ? mode : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( user ) ? user : "-" ).append( "," ).
            append( StringUtils.isNotEmpty( group ) ? group : "-" ).append( ") " ).
            append( path ).toString() );
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
        throws MissingSettingException
    {
        SpecWriter spec = new SpecWriter( writer );

        for ( Iterator it = defineStatements.iterator(); it.hasNext(); )
        {
            spec.add( "%define " + it.next() );
        }

        UnixUtil.assertField( "version", version );
        spec.add( "Name: " + UnixUtil.getField( "name", getName() ) );
        spec.add( "Version: " + getRpmVersion( version ) );
        spec.add( "Release: " + getRpmRelease( version ) );
        spec.add( "Summary: " + UnixUtil.getField( "summary", summary ) );
        spec.add( "License: " + UnixUtil.getField( "license", license ) );
        spec.addIf( "Distribution: ", distribution );
        spec.addIf( "Icon", icon );
        spec.addIf( "Vendor", vendor );
        spec.addIf( "URL", url );
        spec.add( "Group: " + UnixUtil.getField( "group", group ) );
        spec.addIf( "Packager", packager );
        spec.addAll( "Provides", provides );
        spec.addAll( "Requires", requires );
        spec.addAll( "Conflicts", conflicts );

        spec.add( "BuildRoot: " + UnixUtil.getField( "buildRoot", buildRoot ).getAbsolutePath() );
        spec.add();

        // The %description tag is required even if it is empty.
        spec.add( "%description" );
        spec.addIf( StringUtils.isNotEmpty( description ), description );
        spec.add();

        spec.add( "%files" );
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            spec.add( it.next().toString() );
        }

        spec.addIf( includePre != null || includePost != null || includePreun != null || includePostun != null, "" );
        spec.addIf( includePre != null, "%pre" );
        spec.includeIf( includePre );
        spec.addIf( includePost != null, "%post" );
        spec.includeIf( includePost );
        spec.addIf( includePreun != null, "%preun" );
        spec.includeIf( includePreun );
        spec.addIf( includePostun != null, "%postun" );
        spec.includeIf( includePostun );

        spec.addIf( dump, "%dump" );
    }

    private static class SpecWriter
    {
        private PrintWriter writer;

        public SpecWriter( PrintWriter writer )
        {
            this.writer = writer;
        }

        public void add( String string )
        {
            writer.println( string );
        }

        public void addIf( String field, String value )
        {
            if ( StringUtils.isNotEmpty( value ) )
            {
                writer.println( field + ": " + value );
            }
        }

        public void addIf( String field, File file )
        {
            if ( file != null )
            {
                writer.println( field + ": " + file.getAbsolutePath() );
            }
        }

        public void add()
        {
            writer.println();
        }

        public void addIf( boolean flag, String line )
        {
            if ( flag )
            {
                writer.println( line );
            }
        }

        public void addAll( String prefix, List provides )
        {
            if ( provides == null )
            {
                return;
            }

            for ( Iterator iterator = provides.iterator(); iterator.hasNext(); )
            {
                add( prefix + ": " + iterator.next() );
            }
        }

        public void includeIf( File file )
        {
            if ( file != null )
            {
                writer.println( "%include " + file.getAbsolutePath() );
            }
        }
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
