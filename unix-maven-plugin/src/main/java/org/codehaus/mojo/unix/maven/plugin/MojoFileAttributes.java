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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.java.StringF.*;

/**
 * TODO: Re-work how these attributes are validated. Right now a RuntimeException is thrown, but the
 * create() method should return an Either or Option with an validation message on errors.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: FileAttributes.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class MojoFileAttributes
{
    public Option<String> user = none();

    public Option<String> group = none();

    public Option<UnixFileMode> mode = none();

    public List<String> tags = nil();
    
    public MojoFileAttributes()
    {
    }

    public MojoFileAttributes( String user, String group, UnixFileMode mode )
    {
        this.user = fromNull( user );
        this.group = fromNull( group );
        this.mode = fromNull( mode );
    }

    public void setUser( String user )
    {
        this.user = fromNull( user );
    }

    public void setGroup( String group )
    {
        this.group = fromNull( group );
    }

    public void setMode( String mode )
    {
        this.mode = some( UnixFileMode.fromInt( Integer.parseInt( mode, 8 ) ) );
    }

    public void setTags( String tags )
    {
        this.tags = list( tags.split( "," ) ).map( trim );
    }

    public org.codehaus.mojo.unix.FileAttributes create()
    {
        return new org.codehaus.mojo.unix.FileAttributes( user, group, mode, tags );
    }

    public static final F<MojoFileAttributes, org.codehaus.mojo.unix.FileAttributes> create_ =
        new F<MojoFileAttributes, org.codehaus.mojo.unix.FileAttributes>()
        {
            public org.codehaus.mojo.unix.FileAttributes f( MojoFileAttributes fileAttributes )
            {
                return fileAttributes.create();
            }
        };
}
