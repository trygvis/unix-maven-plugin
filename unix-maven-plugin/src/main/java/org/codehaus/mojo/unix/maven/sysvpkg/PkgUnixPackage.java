package org.codehaus.mojo.unix.maven.sysvpkg;

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

import fj.*;
import fj.data.List;
import static fj.data.List.*;
import fj.data.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;

import org.codehaus.mojo.unix.io.*;
import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.sysvpkg.*;
import org.codehaus.mojo.unix.sysvpkg.prototype.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import static org.joda.time.LocalDateTime.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkgUnixPackage
    extends UnixPackage<PkgUnixPackage>
{
    private LocalFs prototype;
    private LocalFs pkginfo;
    private boolean debug;

    private static final ScriptUtil scriptUtil = new ScriptUtil( "preinstall", "postinstall", "preremove", "postremove" ).
        customScript( "depend" ).
        customScript( "checkinstall" ).
        customScript( "compver" ).
        customScript( "copyright" ).
        customScript( "request" ).
        customScript( "space" );

    private PrototypeFile prototypeFile;

    private PkginfoFile pkginfoFile;

    private List<IoEffect> operations = nil();

    private Option<String> classifier = none();

    public PkgUnixPackage()
    {
        super( "pkg" );
    }

    public PkgUnixPackage parameters( PackageParameters parameters )
    {
        this.classifier = parameters.classifier;
        pkginfoFile = new PkginfoFile( parameters.architecture.orSome( "all" ),
                                       "application",
                                       parameters.name,
                                       parameters.id,
                                       getPkgVersion( parameters.version ) ).
            desc( parameters.description ).
            email( parameters.contactEmail ).
            pstamp( some( parameters.version.timestamp ) ).
            email( parameters.contactEmail );

        return this;
    }

    // -----------------------------------------------------------------------
    // Common Settings
    // -----------------------------------------------------------------------

    public PkgUnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException
    {
        prototype = workingDirectory.resolve( relativePath( "prototype" ) );
        pkginfo = workingDirectory.resolve( relativePath( "pkginfo" ) );

        Directory defaultDirectory = directory( BASE, fromDateFields( new Date( 0 ) ), defaultDirectoryAttributes );
        DirectoryEntry directoryEntry = new DirectoryEntry( Option.<String>none(), defaultDirectory );

        prototypeFile = new PrototypeFile( directoryEntry );
    }

    // -----------------------------------------------------------------------
    // Pkg Specific Settings
    // -----------------------------------------------------------------------

    public PkgUnixPackage pkgParameters( List<String> classes, Option<String> category )
    {
        pkginfoFile = pkginfoFile.
            category( category.orSome( pkginfoFile.category ) ).
            classes( classes );

        return this;
    }

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        // -----------------------------------------------------------------------
        // Validate that the prototype looks sane
        // -----------------------------------------------------------------------

        // TODO: This should be more configurable
        RelativePath[] specialPaths = new RelativePath[]{
            BASE,
            relativePath( "/etc" ),
            relativePath( "/etc/opt" ),
            relativePath( "/opt" ),
            relativePath( "/usr" ),
            relativePath( "/var" ),
            relativePath( "/var/opt" ),
        };

        // TODO: This should use setDirectoryAttributes
        for ( RelativePath specialPath : specialPaths )
        {
            if ( prototypeFile.hasPath( specialPath ) )
            {
                // TODO: this should come from a common time object so that all "now" timestamps are the same
                prototypeFile.addDirectory( directory( specialPath, new LocalDateTime(), EMPTY ) );
            }
        }

        // -----------------------------------------------------------------------
        // The shit
        // -----------------------------------------------------------------------

        ScriptUtil.Result result = scriptUtil.
            createExecution( classifier.orSome( "default" ), "pkg", getScripts(), workingDirectory.file, strategy ).
            execute();

        LineStreamUtil.toFile( pkginfoFile.toList(), pkginfo.file );

        String pkg = pkginfoFile.getPkgName( pkginfo.file );

        prototypeFile.addIFileIf( pkginfo.file, "pkginfo" );
        prototypeFile.addIFileIf( result.preInstall, "preinstall" );
        prototypeFile.addIFileIf( result.postInstall, "postinstall" );
        prototypeFile.addIFileIf( result.preRemove, "preremove" );
        prototypeFile.addIFileIf( result.postRemove, "postremove" );
        for ( File file : result.customScripts )
        {
            prototypeFile.addIFileIf( file );
        }

        workingDirectory.resolve( relativePath( "assembly" ) );

        // TODO: Replace this with an Actor-based execution
        for ( IoEffect operation : operations )
        {
            operation.run();
        }

        LineStreamUtil.toFile( prototypeFile, prototype.file );

        new PkgmkCommand().
            setDebug( debug ).
            setOverwrite( true ).
            setDevice( workingDirectory.file ).
            setPrototype( prototype.file ).
            execute();

        new PkgtransCommand().
            setDebug( debug ).
            setAsDatastream( true ).
            setOverwrite( true ).
            execute( workingDirectory.file, packageFile, pkg );
    }

    public static String getPkgVersion( PackageVersion v )
    {
        String version = v.version;

        if ( v.revision.isSome() )
        {
            version += "-" + v.revision.some();
        }

        if ( v.snapshot )
        {
            version += "-" + v.timestamp;
        }

        return version;
    }

    public void addDirectory( UnixFsObject.Directory directory )
        throws IOException
    {
        prototypeFile.addDirectory( directory );
    }

    public void addFile( Fs<?> fromFile, RegularFile file )
        throws IOException
    {
        prototypeFile.addFile( fromFile( fromFile, file ), file );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        prototypeFile.addSymlink( symlink );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        prototypeFile.apply( f );
    }

    public static PkgUnixPackage cast( UnixPackage unixPackage )
    {
        return PkgUnixPackage.class.cast( unixPackage );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public LocalFs fromFile( final Fs<?> fromFile, UnixFsObject.RegularFile file )
    {
        // If it is a file on the local file system, just point the entry in the prototype file to it
        if ( fromFile instanceof LocalFs )
        {
            return (LocalFs) fromFile;
        }

        // Creates a file under the working directory that should match the destination path
        final LocalFs tmpFile = workingDirectory.resolve( file.path );

        operations = operations.cons( new IoEffect()
        {
            public void run()
                throws IOException
            {
                OutputStream outputStream = null;
                try
                {
                    tmpFile.parent().mkdir();
                    tmpFile.copyFrom( fromFile );
                }
                finally
                {
                    IOUtil.close( outputStream );
                }
            }
        } );

        return tmpFile;
    }
}
