package org.codehaus.mojo.unix.sysvpkg;

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
import static fj.Function.*;
import fj.data.List;
import fj.data.*;
import static fj.data.Option.*;
import fj.data.TreeMap;
import static fj.pre.Ord.*;
import org.codehaus.mojo.unix.java.*;
import static org.codehaus.mojo.unix.java.StringF.*;
import org.codehaus.mojo.unix.util.fj.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * A pkginfo file. See <code>man -s 4 pkginfo</code>.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFile
{
    public final String arch;
    // TODO: This should be a list according to the man page
    public final String category;
    public final String name;
    public final String pkg;
    public final String version;
    public final Option<String> pstamp;
    public final Option<String> desc;
    public final Option<String> email;
    public final List<String> classes;

    public PkginfoFile( String arch, String category, String name, String pkg, String version, Option<String> pstamp,
                        Option<String> desc, Option<String> email, List<String> classes )
    {
        this.arch = arch;
        this.category = category;
        this.name = name;
        this.pkg = pkg;
        this.version = version;
        this.pstamp = pstamp;
        this.desc = desc;
        this.email = email;
        this.classes = classes;
    }

    public PkginfoFile( String arch, String category, String name, String pkg, String version )
    {
        this( arch, category, name, pkg, version, Option.<String>none(), Option.<String>none(),
              Option.<String>none(), List.<String>nil() );
    }

    public static final F5<String, String, String, String, String, PkginfoFile> constructor =
        new F5<String, String, String, String, String, PkginfoFile>()
    {
        public PkginfoFile f( String arch, String category, String name, String pkg, String version )
        {
            return new PkginfoFile( arch, category, name, pkg, version );
        }
    };

    public PkginfoFile category( String category )
    {
        return new PkginfoFile( arch, category, name, pkg, version, pstamp, desc, email, classes );
    }

    public PkginfoFile pstamp( Option<String> pstamp )
    {
        return new PkginfoFile( arch, category, name, pkg, version, pstamp, desc, email, classes );
    }

    public PkginfoFile desc( Option<String> desc )
    {
        return new PkginfoFile( arch, category, name, pkg, version, pstamp, desc, email, classes );
    }

    public PkginfoFile email( Option<String> email )
    {
        return new PkginfoFile( arch, category, name, pkg, version, pstamp, desc, email, classes );
    }

    public PkginfoFile classes( List<String> classes )
    {
        return new PkginfoFile( arch, category, name, pkg, version, pstamp, desc, email, classes );
    }

    public List<String> toList()
    {
        F<List<String>, String> folder = List.<String, String>foldLeft().f( joiner.f( " " ) ).f( "" );

        F<List<String>, String> stringF = FunctionF.compose( curry( concat, "CLASSES=" ), trim, folder );

        List<Option<String>> list = List.<Option<String>>single( some( "ARCH=" + arch ) ).
            cons( some( "CATEGORY=" + category ) ).
            cons( some( "NAME=" + name ) ).
            cons( some( "PKG=" + pkg ) ).
            cons( some( "VERSION=" + version ) ).
            cons( pstamp.map( curry( concat, "PSTAMP=" ) ) ).
            cons( desc.map( curry( concat, "DESC=" ) ) ).
            cons( email.map( curry( concat, "EMAIL=" ) ) ).
            cons( iif( List.<String>isNotEmpty_(), classes ).map( stringF ) );

        return Option.somes( list ).reverse();
    }

    public String toString()
    {
        String eol = System.getProperty( "line.separator" );
        return toList().foldLeft( joiner.f( eol ), "" ).trim() + eol;
    }

    public String getPkgName( File pkginfoFile )
        throws IOException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream( pkginfoFile );

            Properties properties = new Properties();
            properties.load( inputStream );

            String pkg = properties.getProperty( "PKG" );

            if ( pkg == null )
            {
                throw new IOException( "Could not read package name (PKG) from pkginfo file: '" +
                    pkginfoFile.getAbsolutePath() + "'." );
            }

            return pkg;
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    public static Option<PkginfoFile> fromStream( Iterable<String> lines )
    {
        TreeMap<String, String> map = TreeMap.empty( stringOrd );

        for ( String line : lines )
        {
            int i = line.indexOf( ':' );

            if ( i == -1 )
            {
                continue;
            }

            String field = line.substring( 0, i ).trim();
            String value = line.substring( i + 1 ).trim();

            map = map.set( field, value );
        }

        final TreeMap<String, String> map2 = map;

        return map.get( "ARCH" ).
            bind( map.get( "CATEGORY" ), map.get( "NAME" ), map.get( "PKG" ).orElse( map.get( "PKGINST" ) ), map.get( "VERSION" ),
                                          curry( PkginfoFile.constructor ) ).map( new F<PkginfoFile, PkginfoFile>()
        {
            public PkginfoFile f( PkginfoFile pkginfoFile )
            {
                return pkginfoFile.
                    pstamp( map2.get( "PSTAMP" ) ).
                    desc( map2.get( "DESC" ) ).
                    email( map2.get( "EMAIL" ) ).
                    classes( map2.get( "CLASSES" ).map( flip( StringF.split ).f( "," ) ).orSome( List.<String>nil() ) );
            }
        } );
    }
}
