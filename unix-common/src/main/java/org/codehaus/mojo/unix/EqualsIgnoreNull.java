package org.codehaus.mojo.unix;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface EqualsIgnoreNull<T>
{
    boolean equalsIgnoreNull( T other );
}
