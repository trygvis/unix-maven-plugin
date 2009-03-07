package org.codehaus.mojo.unix.java;

import fj.F;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StringF
{
    public static final F<String, Boolean> isEmpty = new F<String, Boolean>()
    {
        public Boolean f( final String s )
        {
            return s.length() == 0;
        }
    };
}
