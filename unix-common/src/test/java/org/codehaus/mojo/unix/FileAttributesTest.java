package org.codehaus.mojo.unix;

import static fj.Function.compose;
import static fj.Function.curry;
import static fj.data.Option.fromNull;
import static fj.data.Option.some;
import junit.framework.TestCase;
import static org.codehaus.mojo.unix.FileAttributes.useAsDefaultsFor;
import static org.codehaus.mojo.unix.UnixFileMode.none;
import static org.codehaus.mojo.unix.util.UnixUtil.noneString;

public class FileAttributesTest
    extends TestCase
{
    public void testEquals()
    {
        FileAttributes plain = new FileAttributes();
        assertEquals( plain, plain );

        assertFalse( new FileAttributes( some( "trygve" ), noneString, none ).equals( plain ) );
    }

    public void testSetDefaults()
    {
        assertEquals( new FileAttributes( some("c"), noneString, none ), test( "a", "b", "c" ) );
        assertEquals( new FileAttributes( some("c"), noneString, none ), test( "a", null, "c" ) );
        assertEquals( new FileAttributes( some("c"), noneString, none ), test( null, "b", "c" ) );

        assertEquals( new FileAttributes( some("b"), noneString, none ), test( "a", "b", null ) );
        assertEquals( new FileAttributes( some("a"), noneString, none ), test( "a", null, null ) );
        assertEquals( new FileAttributes( some("b"), noneString, none ), test( null, "b", null ) );
    }

    public FileAttributes test( String defaultDefaults, String defaults, String attributes )
    {
        return compose(
            curry( useAsDefaultsFor, new FileAttributes( fromNull( defaultDefaults ), noneString, none) ),
            curry( useAsDefaultsFor, new FileAttributes( fromNull( defaults ), noneString, none ) ) ).
            f( new FileAttributes( fromNull( attributes ), noneString, none ) );
    }
}
