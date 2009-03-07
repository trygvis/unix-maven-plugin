package org.codehaus.mojo.unix;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface HasFileAttributes<T extends HasFileAttributes>
{
    FileAttributes getFileAttributes();

    T setFileAttributes( FileAttributes attributes );
}
