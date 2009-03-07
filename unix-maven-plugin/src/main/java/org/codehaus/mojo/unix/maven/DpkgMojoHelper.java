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

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.mojo.unix.UnixPackage;
import org.codehaus.mojo.unix.maven.dpkg.DpkgUnixPackage;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class DpkgMojoHelper
    extends MojoHelper
{
    private final DpkgSpecificSettings dpkg;

    public DpkgMojoHelper( DpkgSpecificSettings dpkg )
    {
        this.dpkg = dpkg;
    }

    protected void validateMojoSettings()
        throws MissingSettingException
    {
        if ( dpkg == null )
        {
            throw new MissingSettingException( "You need to specify the required properties when building dpkg packages." );
        }

        if ( StringUtils.isEmpty( dpkg.getPriority() ) )
        {
            dpkg.setPriority( "standard" );
        }

        if ( StringUtils.isEmpty( dpkg.getSection() ) )
        {
            throw new MissingSettingException( "Section has to be specified." );
        }
    }

    protected void applyFormatSpecificSettingsToPackage( UnixPackage unixPackage )
    {
        DpkgUnixPackage.cast( unixPackage ).
            priority( dpkg.getPriority() ).
            section( dpkg.getSection() );
    }
}
