package org.codehaus.mojo.unix.maven.pkg;

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

import fj.F2;
import fj.Unit;
import fj.data.List;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.provider.local.LocalFileSystem;
import org.codehaus.mojo.unix.FileAttributes;
import static org.codehaus.mojo.unix.FileAttributes.EMPTY;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.ScriptUtil;
import org.codehaus.mojo.unix.pkg.PkginfoFile;
import org.codehaus.mojo.unix.pkg.PkgmkCommand;
import org.codehaus.mojo.unix.pkg.PkgtransCommand;
import org.codehaus.mojo.unix.pkg.prototype.PrototypeFile;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.line.LineStreamUtil;
import org.codehaus.mojo.unix.util.vfs.VfsUtil;
import org.codehaus.plexus.util.IOUtil;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

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

    private final static ScriptUtil scriptUtil = new ScriptUtil.ScriptUtilBuilder().
        format( "pkg" ).
        setPreInstall( "preinstall" ).
        setPostInstall( "postinstall" ).
        setPreRemove( "preremove" ).
        setPostRemove( "postremove" ).
        addCustomScript( "depend" ).
        addCustomScript( "checkinstall" ).
        addCustomScript( "compver" ).
        addCustomScript( "copyright" ).
        addCustomScript( "request" ).
        addCustomScript( "space" ).
        build();

    private PrototypeFile prototypeFile = new PrototypeFile();

    private final PkginfoFile pkginfoFile = new PkginfoFile();

    private List<Callable> operations = List.nil();

    public PkgUnixPackage()
    {
        super( "pkg" );
    }

    // -----------------------------------------------------------------------
    // Common Settings
    // -----------------------------------------------------------------------

    public UnixPackage mavenCoordinates( String groupId, String artifactId, String classifier )
    {
        pkginfoFile.classifier = classifier;
        return this;
    }

    public UnixPackage name( String name )
    {
        pkginfoFile.packageName = name;
        return this;
    }

    public UnixPackage shortDescription( String shortDescription )
    {
        pkginfoFile.name = shortDescription;
        return this;
    }

    public UnixPackage description( String description )
    {
        pkginfoFile.desc = description;
        return this;
    }

    public UnixPackage contactEmail( String contactEmail )
    {
        pkginfoFile.email = contactEmail;
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        pkginfoFile.arch = architecture;
        return this;
    }

    public UnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        this.workingDirectory = workingDirectory;
        prototype = workingDirectory.resolveFile( "prototype" );
        pkginfo = workingDirectory.resolveFile( "pkginfo" );
        return this;
    }

    public UnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    // -----------------------------------------------------------------------
    // Pkg Specific Settings
    // -----------------------------------------------------------------------

    public void classes( String classes )
    {
        pkginfoFile.classes = classes;
    }

    public void packageToFile( File packageFile )
        throws Exception
    {
        // -----------------------------------------------------------------------
        // Validate that the prototype looks sane
        // -----------------------------------------------------------------------

        // TODO: This should be more configurable
        RelativePath[] specialPaths = new RelativePath[]{
            fromString( "/" ),
            fromString( "/etc" ),
            fromString( "/opt" ),
            fromString( "/usr" ),
            fromString( "/var" ),
            fromString( "/var/opt" ),
        };

        // TODO: This should use setDirectoryAttributes
        for ( RelativePath specialPath : specialPaths )
        {
            if ( prototypeFile.hasPath( specialPath ) )
            {
                // TODO: this should come from a common time object so that all "now" timestamps are the same
                prototypeFile.addDirectory( UnixFsObject.directory( specialPath, new LocalDateTime(), EMPTY ) );
            }
        }

        // -----------------------------------------------------------------------
        // The shit
        // -----------------------------------------------------------------------

        File workingDirectoryF = VfsUtil.asFile( workingDirectory );
        File pkginfoF = VfsUtil.asFile( pkginfo );
        File prototypeF = VfsUtil.asFile( prototype );

        ScriptUtil.Execution execution = scriptUtil.copyScripts( getBasedir(), workingDirectoryF );
        pkginfoFile.version = getVersion().getMavenVersion();
        pkginfoFile.pstamp = getVersion().timestamp;
        LineStreamUtil.toFile( pkginfoFile, pkginfoF );

        String pkg = pkginfoFile.getPkgName( pkginfoF );

        prototypeFile.addIFileIf( pkginfoF, "pkginfo" );
        prototypeFile.addIFileIf( execution.getPreInstall(), "preinstall" );
        prototypeFile.addIFileIf( execution.getPostInstall(), "postinstall" );
        prototypeFile.addIFileIf( execution.getPreRemove(), "preremove" );
        prototypeFile.addIFileIf( execution.getPostRemove(), "postremove" );
        for ( File file : execution.getCustomScripts() )
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
