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

import org.apache.commons.vfs.*;
import org.codehaus.mojo.unix.util.*;

import java.io.*;

/**
 * Represents a Unix package that has attributes and a set of files to be packaged.
 *
 * TODO: Move to unix-core. Stuff in unix-common should be generic for "unix stuff".
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UnixPackage
    implements FileCollector
{
    private final String packageFileExtension;

    private PackageVersion version;

    private File basedir;

    public UnixPackage( String packageFileExtension )
    {
        this.packageFileExtension = packageFileExtension;
    }

    public abstract UnixPackage parameters( PackageParameters parameters );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public UnixPackage workingDirectory( FileObject file )
        throws FileSystemException
    {
        return this;
    }

    public UnixPackage basedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    public File getScripts()
    {
        return new File( basedir, "src/main/unix/scripts" );
    }

    public UnixPackage debug( boolean debug )
    {
        return this;
    }

    public abstract void beforeAssembly( FileAttributes defaultDirectoryAttributes )
        throws IOException;

    public abstract void packageToFile( File packageFile, ScriptUtil.Strategy strategy )
        throws Exception;

    public UnixPackage setVersion( PackageVersion version )
    {
        this.version = version;
        return this;
    }

    public final PackageVersion getVersion()
    {
        return version;
    }

    public String getPackageFileExtension()
    {
        return packageFileExtension;
    }
}
