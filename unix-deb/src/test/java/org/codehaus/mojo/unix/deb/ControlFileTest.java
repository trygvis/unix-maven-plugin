package org.codehaus.mojo.unix.deb;

import fj.data.*;
import static fj.data.List.*;
import fj.pre.*;
import junit.framework.*;
import static org.codehaus.mojo.unix.deb.ControlFile.*;

public class ControlFileTest
    extends TestCase
{
    Show<List<String>> listShow = Show.listShow( Show.stringShow );
    Equal<List<String>> listEqual = Equal.listEqual( Equal.stringEqual );

    public void testBasic()
    {
        ControlFile controlFile = new ControlFile( "package-name" ).depends( list( "tomcat6", "sun-java6-jdk" ) );

        assertTrue( listEqual.eq( controlFile.toList(), list( "Package: package-name",
            "Depends: tomcat6, sun-java6-jdk" ) ) );
    }

    public void testListToHeader()
    {
        List<String> list;

        assertTrue( listEqual.eq( listToHeader( 10, "Foo", List.<String>nil() ), List.<String>nil() ) );

        assertTrue( listEqual.eq( listToHeader( 10, "Foo", single( "yo" ) ), single( "Foo: yo" ) ) );

        list = listToHeader( 10, "Foo", list( "yo", "1234567890" ) );
        assertTrue( listEqual.eq( list, list( "Foo: yo, ", " 1234567890" ) ) );

        list = listToHeader( 10, "Foo", list( "1234567890", "yo" ) );
        assertTrue( listEqual.eq( list, list( "Foo: 1234567890, ", " yo" ) ) );

        list = listToHeader( 10, "Foo", list( "1234567890", "1234567890", "1234567890", "1234567890", "1234567890" ) );
        assertTrue( listEqual.eq( list, list( "Foo: 1234567890, ", " 1234567890, ", " 1234567890, ", " 1234567890, ", " 1234567890" ) ) );

        list = listToHeader( 10, "Foo", list( "aa", "bb", "cc", "dd", "ee", "ff", "gg", "hh", "ii", "jj" ) );
        assertTrue( listEqual.eq( list, list( "Foo: aa, bb, ", " cc, dd, ee, ", " ff, gg, hh, ", " ii, jj" ) ) );
    }
}
