package org.codehaus.mojo.unix;

import org.apache.commons.vfs.FileObject;

import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface FileCollector
{
    FileCollector addDirectory( String path, String user, String group, String mode )
        throws IOException;

    FileCollector addFile( FileObject fromFile, String toFile, String user, String group, String mode )
        throws IOException;
}
