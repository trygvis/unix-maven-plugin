package org.codehaus.mojo.unix.maven.plugin;

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

import org.apache.maven.plugin.logging.*;
import org.codehaus.mojo.unix.maven.deb.*;
import org.codehaus.mojo.unix.maven.rpm.*;
import org.codehaus.mojo.unix.maven.sysvpkg.*;
import org.codehaus.mojo.unix.maven.zip.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class PackagingFormat<UP>
{
    public abstract UP start( Log log );

    @SuppressWarnings( "unchecked" )
    public static <UP> PackagingFormat<UP> lookup( String key ) {
        if(key.equals( "deb" ) ) {
            return (PackagingFormat<UP>) new DebPackagingFormat();
        }
        if(key.equals( "sysvpkg" ) ) {
            return (PackagingFormat<UP>) new SysvPkgPackagingFormat();
        }
        if(key.equals( "rpm" ) ) {
            return (PackagingFormat<UP>) new RpmPackagingFormat();
        }
        if(key.equals( "zip" ) ) {
            return (PackagingFormat<UP>) new ZipPackagingFormat();
        }

        throw new RuntimeException( "Unknown packaging format: " + key );
    }
}
