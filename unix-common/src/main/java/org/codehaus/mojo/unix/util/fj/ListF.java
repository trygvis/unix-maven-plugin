package org.codehaus.mojo.unix.util.fj;

import fj.*;
import fj.data.*;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ListF
{
    public static <T> F<T[], List<T>> list_()
    {
        return new F<T[], List<T>>()
        {
            public List<T> f( T[] ts )
            {
                return List.list( ts );
            }
        };
    }
}
