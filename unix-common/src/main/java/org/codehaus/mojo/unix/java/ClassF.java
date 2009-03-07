package org.codehaus.mojo.unix.java;

import fj.F;
import fj.F2;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClassF
{
    public static <A, B> F<A, B> cast()
    {
        return new F<A, B>()
        {
            public B f( A a )
            {
                //noinspection unchecked
                return (B) a;
            }
        };
    }

    public static final F<java.lang.Class, Package> getPackage = new F<java.lang.Class, Package>()
    {
        public Package f( java.lang.Class _this )
        {
            return _this.getPackage();
        }
    };

    public static final F<java.lang.Class, java.lang.String> getName = new F<java.lang.Class, String>()
    {
        public String f( java.lang.Class _this )
        {
            return _this.getName();
        }
    };

    public static final F2<java.lang.Class, java.lang.Class, Boolean> isAssignableFrom = new F2<java.lang.Class, java.lang.Class, Boolean>()
    {
        public Boolean f( java.lang.Class _this, java.lang.Class cls )
        {
            return _this.isAssignableFrom( cls );
        }
    };
}
