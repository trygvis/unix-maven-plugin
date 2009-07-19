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
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.local.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.sysvpkg.*;
import org.codehaus.mojo.unix.sysvpkg.prototype.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import static org.joda.time.LocalDateTime.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgUnixPackage
    extends UnixPackage
{
    private FileObject workingDirectory;
    private FileObject prototype;
    private FileObject pkginfo;
    private boolean debug;

    private static final ScriptUtil scriptUtil = new ScriptUtil( "preinstall" , "postinstall" , "preremove" , "postremove" ).
        customScript( "depend" ).
        customScript( "checkinstall" ).
        customScript( "compver" ).
        customScript( "copyright" ).
        customScript( "request" ).
        customScript( "space" );

    private PrototypeFile prototypeFile;

    private PkginfoFile pkginfoFile;

    private List<Callable> operations = nil();

    private Option<String> classifier = none();

    public PkgUnixPackage()
    {
        super( "pkg" );
    }

    public UnixPackage parameters( PackageParameters parameters )
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

    public UnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public UnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes )
        throws IOException
    {
        prototype = workingDirectory.resolveFile( "prototype" );
        pkginfo = workingDirectory.resolveFile( "pkginfo" );

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

        File workingDirectoryF = asFile( workingDirectory );
        File pkginfoF = asFile( pkginfo );
        File prototypeF = asFile( prototype );

        ScriptUtil.Result result = scriptUtil.
            createExecution( classifier.orSome( "default" ), "pkg", getScripts(), workingDirectoryF, strategy ).
            execute();

        LineStreamUtil.toFile( pkginfoFile.toList(), pkginfoF );

        String pkg = pkginfoFile.getPkgName( pkginfoF );

        prototypeFile.addIFileIf( pkginfoF, "pkginfo" );
        prototypeFile.addIFileIf( result.preInstall, "preinstall" );
        prototypeFile.addIFileIf( result.postInstall, "postinstall" );
        prototypeFile.addIFileIf( result.preRemove, "preremove" );
        prototypeFile.addIFileIf( result.postRemove, "postremove" );
        for ( File file : result.customScripts )
        {
            prototypeFile.addIFileIf( file );
        }

        workingDirectory.resolveFile( "assembly" );

        // TODO: Replace this with an Actor-based execution
        for ( Callable operation : operations )
        {
            operation.call();
        }

        LineStreamUtil.toFile( prototypeFile, prototypeF );

        new PkgmkCommand().
            setDebug( debug ).
            setOverwrite( true ).
            setDevice( workingDirectoryF ).
            setPrototype( prototypeF ).
            execute();

        new PkgtransCommand().
            setDebug( debug ).
            setAsDatastream( true ).
            setOverwrite( true ).
            execute( workingDirectoryF, packageFile, pkg );
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

    public FileObject getRoot()
    {
        return workingDirectory;
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
        throws IOException
    {
        prototypeFile.addDirectory( directory );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
        throws IOException
    {
        prototypeFile.addFile( fromFile( fromFile, file ), file  );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        prototypeFile.addSymlink( symlink );

        return this;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
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

    public FileObject fromFile( final FileObject fromFile, UnixFsObject.RegularFile file )
        throws FileSystemException
    {
        // If it is a file on the local file system, just point the entry in the prototype file to it
        if ( fromFile.getFileSystem() instanceof LocalFileSystem )
        {
            return fromFile;
        }

        // Creates a file under the working directory that should match the destination path
        final FileObject tmpFile = workingDirectory.resolveFile( file.path.string );

        operations = operations.cons( new Callable()
        {
            public Object call()
                throws Exception
            {
                OutputStream outputStream = null;
                try
                {
                    tmpFile.getParent().createFolder();
                    tmpFile.copyFrom( fromFile, Selectors.SELECT_ALL );
                    tmpFile.getContent().setLastModifiedTime( fromFile.getContent().getLastModifiedTime() );
                }
                finally
                {
                    IOUtil.close( outputStream );
                }

                return Unit.unit();
            }
        });

        return tmpFile;
    }
}
