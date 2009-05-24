package org.codehaus.mojo.unix.maven.dpkg;

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
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.dpkg.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackage
    extends UnixPackage
{
    private static final String EOL = System.getProperty( "line.separator" );

    private ControlFile controlFile;

    private FileObject workingDirectory;

    private FsFileCollector fileCollector;

    private String dpkgDebPath;

    private boolean debug;

    private final static ScriptUtil scriptUtil = new ScriptUtil( "preinst", "postinst", "prerm", "postrm" );

    public DpkgUnixPackage()
    {
        super( "deb" );
    }

    public DpkgUnixPackage parameters( PackageParameters parameters )
    {
        controlFile = new ControlFile();
        controlFile.packageName = parameters.id;
        controlFile.description = getDescription( parameters );
        if ( parameters.contact.isSome() )
        {
            controlFile.maintainer = parameters.contact.some();
        }
        controlFile.architecture = parameters.architecture.orSome( "all" );
        controlFile.version = parameters.version;

        return this;
    }

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
        fileCollector = new FsFileCollector( workingDirectory.resolveFile( "assembly" ) );
    }

    public FileObject getRoot()
    {
        return fileCollector.getRoot();
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
    {
        fileCollector.addDirectory( directory );

        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
    {
        fileCollector.addFile( fromFile, file );

        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        fileCollector.addSymlink( symlink );

        return this;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        fileCollector.apply( f );
    }

    // -----------------------------------------------------------------------
    // Debian Specifics
    // -----------------------------------------------------------------------

    public DpkgUnixPackage dpkgDeb( String dpkgDeb )
    {
        this.dpkgDebPath = dpkgDeb;
        return this;
    }

    public DpkgUnixPackage section( String secion )
    {
        controlFile.section = secion;
        return this;
    }

    public DpkgUnixPackage priority( String priority )
    {
        controlFile.priority = priority;
        return this;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        FileObject fsRoot = fileCollector.getFsRoot();
        FileObject debian = fsRoot.resolveFile( "DEBIAN" );
        FileObject controlFilePath = debian.resolveFile( "control" );

        debian.createFolder();
        LineStreamUtil.toFile( controlFile, asFile( controlFilePath ) );

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
            createExecution( controlFile.packageName, "dpkg", getScripts(), asFile( debian ), strategy ).
            execute();

        UnixUtil.chmodIf( result.preInstall, "0755" );
        UnixUtil.chmodIf( result.postInstall, "0755" );
        UnixUtil.chmodIf( result.preRemove, "0755" );
        UnixUtil.chmodIf( result.postRemove, "0755" );

        new Dpkg().
            setDebug( debug ).
            setPackageRoot( asFile( fsRoot ) ).
            setDebFile( packageFile ).
            setDpkgDebPath( dpkgDebPath ).
            execute();
    }

    public static DpkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (DpkgUnixPackage) unixPackage;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static String getDescription( PackageParameters parameters )
    {
        String description = "";

        if ( parameters.name.isSome() )
        {
            description = parameters.name.some().trim();
        }

        if ( parameters.description.isSome() )
        {
            description = description + EOL + parameters.description.some().trim();
        }

        // ----------------------------------------------------------------------
        // Trim each line, replace blank lines with " ."
        // ----------------------------------------------------------------------

        String debianDescription;

        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( description.trim() ) );

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
