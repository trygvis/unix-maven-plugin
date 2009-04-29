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
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.maven.*;
import org.codehaus.plexus.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: DpkgMojoHelper.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class DpkgMojoUtil
{
    public static final F2<DpkgSpecificSettings, UnixPackage, UnixPackage> validateMojoSettingsAndApplyFormatSpecificSettingsToPackage =
        new F2<DpkgSpecificSettings, UnixPackage, UnixPackage>()
        {
            public UnixPackage f( DpkgSpecificSettings dpkgSpecificSettings, UnixPackage unixPackage )
            {
                return validateMojoSettingsAndApplyFormatSpecificSettingsToPackage( dpkgSpecificSettings, unixPackage );
            }
        };

    public static UnixPackage validateMojoSettingsAndApplyFormatSpecificSettingsToPackage( DpkgSpecificSettings dpkg,
                                                                                           UnixPackage unixPackage )
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

        // TODO: Move this
//        if ( mojoParameters.revision.isSome() )
//        {
//            try
//            {
//                Integer.parseInt( mojoParameters.revision.some() );
//            }
//            catch ( NumberFormatException e )
//            {
//                throw new MissingSettingException( "The revision field has to be an integer for DPKG packages." );
//            }
//        }

        return DpkgUnixPackage.cast( unixPackage ).
            priority( dpkg.getPriority() ).
            section( dpkg.getSection() );
    }
}
