package org.codehaus.mojo.unix.util.line;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface LineProducer
{
    void streamTo( LineStreamWriter streamWriter);
}
