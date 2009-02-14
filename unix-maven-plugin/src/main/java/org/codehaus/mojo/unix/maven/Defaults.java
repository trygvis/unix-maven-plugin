package org.codehaus.mojo.unix.maven;

import org.codehaus.mojo.unix.UnixFileMode;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Defaults
{
    public final static org.codehaus.mojo.unix.FileAttributes DEFAULT_FILE_ATTRIBUTES = 
        new org.codehaus.mojo.unix.FileAttributes( "nobody", "nogroup", UnixFileMode._0644 );

    public final static org.codehaus.mojo.unix.FileAttributes DEFAULT_DIRECTORY_ATTRIBUTES =
        new org.codehaus.mojo.unix.FileAttributes( "nobody", "nogroup", UnixFileMode._0755 );

    private FileAttributes fileAttributes = new FileAttributes();

    private FileAttributes directoryAttributes = new FileAttributes();

    public org.codehaus.mojo.unix.FileAttributes getFileAttributes()
    {
        return fileAttributes.create();
    }

    public void setFile( FileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    public org.codehaus.mojo.unix.FileAttributes getDirectoryAttributes()
    {
        return directoryAttributes.create();
    }

    public void setDirectoryAttributes( FileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }
}
