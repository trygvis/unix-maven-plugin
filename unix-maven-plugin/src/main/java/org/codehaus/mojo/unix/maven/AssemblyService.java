package org.codehaus.mojo.unix.maven;

import org.apache.commons.vfs.FileObject;

import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface AssemblyService
{
    String ROLE = AssemblyService.class.getName();

    void copyFile( FileObject from, String to )
        throws IOException;
}
