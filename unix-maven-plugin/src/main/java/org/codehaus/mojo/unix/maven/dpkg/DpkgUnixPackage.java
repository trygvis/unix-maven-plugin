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
import fj.data.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.dpkg.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.vfs.VfsUtil.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgUnixPackage
    extends UnixPackage
{
    private final ControlFile controlFile = new ControlFile();

    private FileObject workingDirectory;

    private FsFileCollector fileCollector;

    private String dpkgDebPath;

    private boolean debug;

    private String id;

    private final static ScriptUtil scriptUtil = new ScriptUtil( "preinst", "postinst", "prerm", "postrm" );

    public DpkgUnixPackage()
    {
        super( "deb" );
    }

    public UnixPackage mavenCoordinates( String groupId, String artifactId )
    {
        controlFile.groupId = groupId;
        controlFile.artifactId = artifactId;

        return this;
    }

    public UnixPackage id( String id )
    {
        this.id = id;
        return this;
    }

    public UnixPackage name( Option<String> name )
    {
//        controlFile._package = name;
        controlFile.shortDescription = name.orSome( "" ); // TODO: This is not right
        return this;
    }

    public UnixPackage description( Option<String> description )
    {
        controlFile.description = description.orSome( "" ); // TODO: This is not right
        return this;
    }

    public UnixPackage contact( Option<String> contact )
    {
        if ( contact.isSome() )
        {
            controlFile.maintainer = contact.some();
        }
        return this;
    }

    public UnixPackage architecture( String architecture )
    {
        controlFile.architecture = architecture;
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
        File assembly = asFile( fileCollector.getFsRoot() );
        controlFile.version = getVersion();
        controlFile.toFile( assembly );

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
            createExecution( id, "dpkg", getScripts(), new File( assembly, "DEBIAN" ), strategy ).
            execute();

        UnixUtil.chmodIf( result.preInstall, "0755" );
        UnixUtil.chmodIf( result.postInstall, "0755" );
        UnixUtil.chmodIf( result.preRemove, "0755" );
        UnixUtil.chmodIf( result.postRemove, "0755" );

        new Dpkg().
            setDebug( debug ).
            setPackageRoot( assembly ).
            setDebFile( packageFile ).
            setDpkgDebPath( dpkgDebPath ).
            execute();
    }

    public static DpkgUnixPackage cast( UnixPackage unixPackage )
    {
        return (DpkgUnixPackage) unixPackage;
    }
}
