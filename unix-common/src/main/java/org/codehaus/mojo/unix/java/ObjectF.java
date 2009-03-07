package org.codehaus.mojo.unix.java;

import fj.F;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ObjectF
{
    public static <T> F<T, Class> getClass_()
    {
        return new F<T, Class>()
        {
            public Class f( T o )
            {
                return o.getClass();
            }
        };
    }
}
