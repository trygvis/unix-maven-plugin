package org.codehaus.mojo.unix.io.fs;

import java.io.*;

public class FsUtil
{
    /**
     * Remember to close() the Fs-es after use.
     */
    public static Fs<?> resolve( File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return new LocalFs( file );
        }
        else if ( file.getName().endsWith( ".jar" ) || file.getName().endsWith( ".zip" ) )
        {
            return new ZipFsRoot( file );
        }
        else
        {
            throw new IOException( "Unable to resolve file type: " + file.getAbsolutePath() );
        }
    }
}
