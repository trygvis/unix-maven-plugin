package org.codehaus.mojo.unix.maven.rpm;

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
import static fj.P.*;
import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.rpm.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import org.codehaus.mojo.unix.util.*;
import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.mojo.unix.util.vfs.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmUnixPackage
    extends UnixPackage
{
    private SpecFile specFile;

    private FsFileCollector fileCollector;

    private FileObject workingDirectory;

    private String rpmbuildPath;

    private boolean debug;

    private final static ScriptUtil scriptUtil = new ScriptUtil( "pre", "post", "preun", "postun" );

    public RpmUnixPackage()
    {
        super( "rpm" );
    }

    public RpmUnixPackage parameters( PackageParameters parameters )
    {
        if ( parameters.license.isNone() )
        {
            throw new MissingSettingException( "The project has to specify a license." );
        }

        specFile = new SpecFile();
        specFile.name = parameters.id;
        specFile.summary = parameters.name;
        specFile.description = parameters.description.orSome( "" ); // TODO: This is not right
        specFile.license = parameters.license.some();

        P2<String, String> rpmVersion = getRpmVersion( parameters.version );
        specFile.version = rpmVersion._1();
        specFile.release = rpmVersion._2();

        return this;
    }

    public UnixPackage group( String group )
    {
        specFile.group = group;
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
        this.specFile.dump = debug;
        this.debug = debug;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes )
        throws IOException
    {
        specFile.beforeAssembly( directory( BASE, new LocalDateTime(), defaultDirectoryAttributes ) );
        fileCollector = new FsFileCollector( workingDirectory.resolveFile( "assembly" ) );
    }

    // TODO: This is not used
    public UnixPackage rpmbuildPath( String rpmbuildPath )
    {
        this.rpmbuildPath = rpmbuildPath;
        return this;
    }

    public FileObject getRoot()
    {
        return fileCollector.getRoot();
    }

    public FileCollector addDirectory( UnixFsObject.Directory directory )
        throws IOException
    {
        specFile.addDirectory( directory );
        fileCollector.addDirectory( directory );
        return this;
    }

    public FileCollector addFile( FileObject fromFile, UnixFsObject.RegularFile file )
        throws IOException
    {
        specFile.addFile( file );
        fileCollector.addFile( fromFile, file );
        return this;
    }

    public FileCollector addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        specFile.addSymlink( symlink );
        fileCollector.addSymlink( symlink );

        return this;
    }

    public void apply( F2<UnixFsObject, FileAttributes, FileAttributes> f )
    {
        specFile.apply( f );
    }

    public void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception
    {
        File workingDirectoryF = VfsUtil.asFile( workingDirectory );
        File rpms = new File( workingDirectoryF, "RPMS" );
        File specsDir = new File( workingDirectoryF, "SPECS" );
        File tmp = new File( workingDirectoryF, "tmp" );
        
        File specFilePath = new File( specsDir, specFile.name + ".spec" );

        FileUtils.forceMkdir( new File( workingDirectoryF, "BUILD" ) );
        FileUtils.forceMkdir( rpms );
        FileUtils.forceMkdir( new File( workingDirectoryF, "SOURCES" ) );
        FileUtils.forceMkdir( specsDir );
        FileUtils.forceMkdir( new File( workingDirectoryF, "SRPMS" ) );
        FileUtils.forceMkdir( tmp );

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
            createExecution( specFile.name, "rpm", getScripts(), workingDirectoryF, strategy ).
            execute();

        specFile.includePre = result.preInstall;
        specFile.includePost = result.postInstall;
        specFile.includePreun = result.preRemove;
        specFile.includePostun = result.postRemove;
        specFile.buildRoot = VfsUtil.asFile( fileCollector.getFsRoot() );

        LineStreamUtil.toFile( specFile, specFilePath );

        new Rpmbuild().
            setDebug( debug ).
            setBuildroot( VfsUtil.asFile( fileCollector.getFsRoot() ) ).
            define( "_tmppath " + tmp.getAbsolutePath() ).
            define( "_topdir " + workingDirectoryF.getAbsolutePath() ).
            define( "_rpmdir " + packageFile.getParentFile().getAbsolutePath() ).
            define( "_rpmfilename " + packageFile.getName() ).
            setSpecFile( specFilePath ).
            setRpmbuildPath( rpmbuildPath ).
            buildBinary();
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RpmUnixPackage cast( UnixPackage unixPackage )
    {
        return (RpmUnixPackage) unixPackage;
    }

    public static P2<String, String> getRpmVersion( PackageVersion version )
    {
        String rpmVersionString = version.version;

        if ( version.snapshot )
        {
            rpmVersionString += "_" + version.timestamp;
        }

        return p( rpmVersionString.replace( '-', '_' ), version.revision.orSome( "1" ) );
    }
}
