package org.codehaus.mojo.unix.util;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: IEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class Validate
{
    public static void validateNotNull( Object... os )
    {
        for ( Object o : os )
        {
            if ( o == null )
            {
                throw new NullPointerException();
            }
        }
    }

    public static void validateNotNull( Object o )
    {
        if ( o == null )
        {
            throw new NullPointerException();
        }
    }

    public static void validateNotNull( Object o, String message )
    {
        if ( o == null )
        {
            throw new NullPointerException( message );
        }
    }
}
