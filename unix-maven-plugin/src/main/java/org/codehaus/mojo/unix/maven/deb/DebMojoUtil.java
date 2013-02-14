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

import static fj.Function.*;
import fj.data.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.java.*;
import org.codehaus.mojo.unix.maven.plugin.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DebMojoUtil
{
    public static DebUnixPackage validateMojoSettingsAndApplyFormatSpecificSettingsToPackage(DebSpecificSettings deb,
                                                                                             DebUnixPackage unixPackage )
    {
        if ( deb == null )
        {
            throw new MissingSettingException(
                "You need to specify the required properties when building deb packages." );
        }

        if ( deb.section.isNone() )
        {
            throw new MissingSettingException( "Section has to be specified." );
        }

        return unixPackage.
            debParameters( fromNull( deb.priority.orSome( "standard" ) ), fromNull( deb.section.some() ),
                           deb.useFakeroot, deb.dpkgDeb,
                           deb.depends.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ),
                           deb.recommends.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ),
                           deb.suggests.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ),
                           deb.preDepends.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ),
                           deb.provides.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ),
                           deb.replaces.map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ) );
    }
}
