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

import static fj.Bottom.error;
import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.join;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.PackageFileSystem;
import org.codehaus.mojo.unix.PackageVersion;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.util.UnixUtil;
import org.codehaus.mojo.unix.util.line.LineProducer;
import static org.codehaus.mojo.unix.util.line.LineStreamUtil.prefix;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Comparator;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFile
    implements LineProducer
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

    public List<String> defineStatements = List.nil();

    public List<String> provides = List.nil();

    public List<String> requires = List.nil();

    public List<String> conflicts = List.nil();

    public String prefix;

    public File buildRoot;

    public String description;

    public boolean dump;

//    private List<UnixFsObject<?>> files = List.nil();

    private final PackageFileSystem fileSystem = new PackageFileSystem( new Comparator<UnixFsObject>()
    {
        public int compare( UnixFsObject a, UnixFsObject b )
        {
            return a.compareTo( b );
        }
    } );

    public File includePre;

    public File includePost;

    public File includePreun;

    public File includePostun;

    public void addFile( UnixFsObject.RegularFile file )
    {
        fileSystem.addFile( file, file );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        fileSystem.addDirectory( directory, directory );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        fileSystem.addSymlink( symlink, symlink );
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        fileSystem.apply( f );
    }

    public void streamTo( LineStreamWriter spec )
    {
        for ( String defineStatement : defineStatements )
        {
            spec.add( "%define " + defineStatement );
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
            addAllLines( prefix( provides, "Provides" ) ).
            addAllLines( prefix( requires, "Requires" ) ).
            addAllLines( prefix( conflicts, "Conflicts" ) ).
            add( "BuildRoot: " + UnixUtil.getField( "buildRoot", buildRoot ).getAbsolutePath() ).
            add();

        // The %description tag is required even if it is empty.
        spec.
            add( "%description" ).
            addIf( StringUtils.isNotEmpty( description ), description ).
            add();

        spec.
            add( "%files" ).
            addAllLines( UnixUtil.iteratorMap( SpecFile.showUnixFsObject(), fileSystem.iterator() ) );

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

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static F<UnixFsObject, String> showUnixFsObject()
    {
        return new F<UnixFsObject, String>()
        {
            public String f( UnixFsObject unixFsObject )
            {
                Option<FileAttributes> attributes = unixFsObject.attributes;

                String s =
                    "%attr(" +
                        join( attributes.map( FileAttributes.modeF ) ).map( UnixFileMode.showOcalString ).orSome( "-" ) + "," +
                        join( attributes.map( FileAttributes.userF ) ).orSome( "-" ) + "," +
                        join( attributes.map( FileAttributes.groupF ) ).orSome( "-" ) + ") " +
                        unixFsObject.path.asAbsolutePath( "/" );

                if ( unixFsObject instanceof UnixFsObject.RegularFile || unixFsObject instanceof UnixFsObject.Symlink )
                {
                    return s;
                }
                else if ( unixFsObject instanceof UnixFsObject.Directory )
                {
                    return "%dir " + s;
                }

                throw error( "Unknown type UnixFsObject type: " + unixFsObject );
            }
        };
    }
}
