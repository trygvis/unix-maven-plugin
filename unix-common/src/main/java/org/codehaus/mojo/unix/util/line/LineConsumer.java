package org.codehaus.mojo.unix.util.line;

/**
 * A factory for objects that can create an object from a set of lines.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface LineConsumer<T>
{
    T fromStream( Iterable<String> lines );
}
