package org.codehaus.mojo.unix.maven.deb;

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
import fj.data.*;
import static fj.data.Option.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.deb.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DebUnixPackage
    extends UnixPackage
{
    private static final String EOL = System.getProperty( "line.separator" );

    private ControlFile controlFile;

    private FileObject workingDirectory;

    private FsFileCollector fileCollector;

    private boolean useFakeroot;

    private boolean debug;

    private final static ScriptUtil scriptUtil = new ScriptUtil( "preinst", "postinst", "prerm", "postrm" );

    public DebUnixPackage()
    {
        super( "deb" );
    }

    public DebUnixPackage parameters( PackageParameters parameters )
    {
        controlFile = new ControlFile(parameters.id).
            version( some( getDebianVersion( parameters.version ) ) ).
            description( getDescription( parameters ) ).
            maintainer( parameters.contact ).
            architecture( parameters.architecture.orElse( some( "all" ) ) );

        return this;
    }

    // TODO: Add paths to dpkg-deb and fakeroot
    public DebUnixPackage debParameters( Option<String> priority,
                                         Option<String> section,
                                         boolean useFakeroot,
                                         List<String> depends, 
                                         List<String> recommends,
                                         List<String> suggests,
                                         List<String> preDepends,
                                         List<String> provides,
                                         List<String> replaces )
    {
        this.useFakeroot = useFakeroot;
        controlFile = controlFile.
            priority( priority ).
            section( section ).
            depends( depends ).
            recommends( recommends ).
            suggests( suggests ).
            preDepends( preDepends ).
            provides( provides ).
            replaces( replaces );

        return this;
    }

    public DebUnixPackage workingDirectory( FileObject workingDirectory )
        throws FileSystemException
    {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public DebUnixPackage debug( boolean debug )
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
    //
    // -----------------------------------------------------------------------

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        FileObject fsRoot = fileCollector.getFsRoot();
        FileObject debian = fsRoot.resolveFile( "DEBIAN" );
        FileObject controlFilePath = debian.resolveFile( "control" );

        debian.createFolder();
        LineStreamUtil.toFile( controlFile.toList(), asFile( controlFilePath ) );

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
            createExecution( controlFile.packageName, "deb", getScripts(), asFile( debian ), strategy ).
            execute();

        UnixUtil.chmodIf( result.preInstall, "0755" );
        UnixUtil.chmodIf( result.postInstall, "0755" );
        UnixUtil.chmodIf( result.preRemove, "0755" );
        UnixUtil.chmodIf( result.postRemove, "0755" );

        new Dpkg().
            setDebug( debug ).
            setPackageRoot( asFile( fsRoot ) ).
            setDebFile( packageFile ).
            setUseFakeroot( useFakeroot ).
            execute();
    }

    public static DebUnixPackage cast( UnixPackage unixPackage )
    {
        return (DebUnixPackage) unixPackage;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static Option<String> getDescription( PackageParameters parameters )
    {
        String description;

        description = parameters.name.trim();

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

        return some( debianDescription );
    }

    public static String getDebianVersion( PackageVersion version )
    {
        String v = version.version;

        if ( version.revision.isSome() )
        {
            // It is assumed that this is validated elsewhere (in the deb mojo helper to be specific)
            v += "-" + Integer.parseInt( version.revision.some() );
        }

        if ( !version.snapshot )
        {
            return v;
        }

        return v + "-" + version.timestamp;
    }
}
