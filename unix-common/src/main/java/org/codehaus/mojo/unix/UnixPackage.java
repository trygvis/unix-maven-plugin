package org.codehaus.mojo.unix;

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

import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.util.*;
import org.joda.time.*;

import java.io.*;

/**
 * Represents a Unix package that has attributes and a set of files to be packaged.
 * <p/>
 * TODO: Move to unix-core. Stuff in unix-common should be generic for "unix stuff".
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class UnixPackage<UP extends UnixPackage<UP, PP>, PP extends UnixPackage.PreparedPackage>
    implements FileCollector
{
    private final String packageFileExtension;

    private PackageVersion version;

    protected LocalFs workingDirectory;

    private File basedir;

    public UnixPackage( String packageFileExtension )
    {
        this.packageFileExtension = packageFileExtension;
    }

    public abstract UP parameters( PackageParameters parameters );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public UP workingDirectory( LocalFs workingDirectory )
    {
        this.workingDirectory = workingDirectory;
        return (UP)this;
    }

    @SuppressWarnings( "unchecked" )
    public UP basedir( File basedir )
    {
        this.basedir = basedir;
        return (UP)this;
    }

    public File getScripts()
    {
        return new File( basedir, "src/main/unix/scripts" );
    }

    @SuppressWarnings( "unchecked" )
    public UP debug( boolean debug )
    {
        return (UP)this;
    }
    @SuppressWarnings( "unchecked" )
    public UP setVersion( PackageVersion version )
    {
        this.version = version;
        return (UP)this;
    }

    public abstract void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException;

    public abstract PP prepare( ScriptUtil.Strategy strategy )
        throws Exception;

    public final PackageVersion getVersion()
    {
        return version;
    }

    public String getPackageFileExtension()
    {
        return packageFileExtension;
    }

    public abstract class PreparedPackage
    {
        public abstract void packageToFile( File packageFile )
            throws Exception;
    }
}
