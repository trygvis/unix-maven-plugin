package org.codehaus.mojo.unix.maven;

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

import fj.F;
import static fj.data.Option.fromNull;
import org.codehaus.mojo.unix.UnixFileMode;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FileAttributes
{
    public String user;

    public String group;

    public UnixFileMode mode = null;

    public FileAttributes()
    {
    }

    // Package protected for Defaults
    FileAttributes( String user, String group, UnixFileMode mode )
    {
        this.user = user;
        this.group = group;
        this.mode = mode;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public UnixFileMode getMode()
    {
        return mode;
    }

    public void setMode( String mode )
    {
        this.mode = UnixFileMode.fromInt( Integer.parseInt( mode, 8 ) );
    }

    public org.codehaus.mojo.unix.FileAttributes create()
    {
        return new org.codehaus.mojo.unix.FileAttributes( fromNull( user ), fromNull( group ), fromNull( mode ) );
    }

    public static final F<FileAttributes, org.codehaus.mojo.unix.FileAttributes> create_ = new F<FileAttributes, org.codehaus.mojo.unix.FileAttributes>()
    {
        public org.codehaus.mojo.unix.FileAttributes f( FileAttributes fileAttributes )
        {
            return fileAttributes.create();
        }
    };
}
