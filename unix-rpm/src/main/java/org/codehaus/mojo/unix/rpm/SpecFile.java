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

import static fj.Bottom.*;
import fj.*;
import static fj.Function.*;
import fj.data.List;
import fj.data.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import static org.codehaus.mojo.unix.PackageFileSystem.*;
import org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.java.FileF.*;
import static org.codehaus.mojo.unix.java.StringF.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.line.LineStreamUtil.*;
import org.codehaus.plexus.util.*;
import static org.joda.time.LocalDateTime.*;
import org.joda.time.*;

import java.io.*;
import java.util.*;

/**
 * TODO: Split this file into two parts; a part which has all the meta data and one that has all the file data.
 *
 * A purely meta data one is useful for parts of the code that just create SPEC files and testing.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SpecFile
    implements LineProducer
{
    public String version;

    public String release;

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

    // Create a default default file system for testing
    private final UnixFsObject DEFAULT_FS_ROOT = UnixFsObject.directory( RelativePath.BASE, LocalDateTime.fromDateFields( new Date() ), EMPTY );
    private final UnixFsObject DEFAULT_DEFAULT = UnixFsObject.directory( RelativePath.BASE, LocalDateTime.fromDateFields( new Date() ), EMPTY );

    private PackageFileSystem<Object> fileSystem = create( new PlainPackageFileSystemObject( DEFAULT_FS_ROOT ),
                                                           new PlainPackageFileSystemObject( DEFAULT_DEFAULT ) );

    public Option<File> includePre = none();

    public Option<File> includePost = none();

    public Option<File> includePreun = none();

    public Option<File> includePostun = none();

    public void beforeAssembly( Directory defaultDirectory )
    {
        Validate.validateNotNull( defaultDirectory );

        Directory root = UnixFsObject.directory( BASE, fromDateFields( new Date( 0 ) ), EMPTY );

        fileSystem = create( new PlainPackageFileSystemObject( root ),
                             new PlainPackageFileSystemObject( defaultDirectory ) );
    }

    public void addFile( UnixFsObject.RegularFile file )
    {
        fileSystem = fileSystem.addFile( new PlainPackageFileSystemObject( file ) );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        fileSystem = fileSystem.addDirectory( new PlainPackageFileSystemObject( directory ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        fileSystem = fileSystem.addSymlink( new PlainPackageFileSystemObject( symlink ) );
    }

    public PackageFileSystem<Object> getFileSystem()
    {
        return fileSystem;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    public void streamTo( LineStreamWriter spec )
    {
        for ( String defineStatement : defineStatements )
        {
            spec.add( "%define " + defineStatement );
        }

        UnixUtil.assertField( "version", version );
        UnixUtil.assertField( "release", release );

        spec.
            add( "Name: " + name ).
            add( "Version: " + version ).
            add( "Release: " + release ).
            add( "Summary: " + UnixUtil.getField( "summary", summary ) ).
            add( "License: " + UnixUtil.getField( "license", license ) ).
            addIfNotEmpty( "Distribution: ", distribution ).
            add( "Group: " + UnixUtil.getField( "group", group ) ).
            addIfNotEmpty( "Packager", packager ).
            addAllLines( prefix( provides, "Provides" ) ).
            addAllLines( prefix( requires, "Requires" ) ).
            addAllLines( prefix( conflicts, "Conflicts" ) ).
            addIfNotEmpty( fromNull( buildRoot ).map( compose( curry( concat, "BuildRoot: " ), getAbsolutePath ) ).orSome( "" ) ).
            add();

        // The %description tag is required even if it is empty.
        spec.
            add( "%description" ).
            addIf( StringUtils.isNotEmpty( description ), description ).
            add();

        spec.
            add( "%files" ).
            addAllLines( fileSystem.prettify().toList().filter( excludePaths ).map( SpecFile.showUnixFsObject() ) );

        spec.addIf( includePre.isSome() || includePost.isSome() || includePreun.isSome() || includePostun.isSome(), "" );
        if ( includePre.isSome() )
        {
            spec.add( "%pre" );
            spec.add( "%include " + includePre.map( getAbsolutePath ).some() );
        }
        if ( includePost.isSome() )
        {
            spec.add( "%post" );
            spec.add( "%include " + includePost.map( getAbsolutePath ).some() );
        }
        if ( includePreun.isSome() )
        {
            spec.add( "%preun" );
            spec.add( "%include " + includePreun.map( getAbsolutePath ).some() );
        }
        if ( includePostun.isSome() )
        {
            spec.add( "%postun" );
            spec.add( "%include " + includePostun.map( getAbsolutePath ).some() );
        }

        spec.addIf( dump, "%dump" );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static <A extends UnixFsObject> F<PackageFileSystemObject<Object>, String> showUnixFsObject()
    {
        return new F<PackageFileSystemObject<Object>, String>()
        {
            public String f( PackageFileSystemObject p2 )
            {
                @SuppressWarnings({"unchecked"}) UnixFsObject<A> unixFsObject = p2.getUnixFsObject();
                Option<FileAttributes> attributes = unixFsObject.attributes;

                String s = "";

                s += unixFsObject.attributes.map( tagsF ).bind( formatTags ).orSome( "" );

                s += "%attr(" +
                    attributes.bind( FileAttributes.modeF ).map( UnixFileMode.showOcalString ).orSome( "-" ) + "," +
                    attributes.bind( FileAttributes.userF ).orSome( "-" ) + "," +
                    attributes.bind( FileAttributes.groupF ).orSome( "-" ) + ") ";

                s += unixFsObject.path.asAbsolutePath( "/" );

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

    private static final F<PackageFileSystemObject<Object>, Boolean> excludePaths = new F<PackageFileSystemObject<Object>, Boolean>()
    {
        public Boolean f( PackageFileSystemObject object )
        {
            return !object.getUnixFsObject().path.isBase();
        }
    };

    private static final F<List<String>, Option<String>> formatTags = new F<List<String>, Option<String>>()
    {
        public Option<String> f( List<String> tags )
        {
            if ( tags.find( curry( equals, "config" ) ).isSome() )
            {
                return some( "%config " );
            }

            if ( tags.find( curry( equals, "rpm:noreplace" ) ).isSome() )
            {
                return some( "%config(noreplace) " );
            }

            if ( tags.find( curry( equals, "rpm:missingok" ) ).isSome() )
            {
                return some( "%config(missingok) " );
            }

            if ( tags.find( curry( equals, "doc" ) ).isSome() )
            {
                return some( "%doc " );
            }

            if ( tags.find( curry( equals, "rpm:ghost" ) ).isSome() )
            {
                return some( "%ghost " );
            }

            return none();
        }
    };
}
