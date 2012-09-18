package org.codehaus.mojo.unix.io.fs;

import java.io.File;
import java.io.IOException;

public class FsUtil
{
    public static Fs<?> resolve( File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return new LocalFs( file );
        }
//        else if ( file.getName().endsWith(".zip") )
//        {
//            return new ZipFs( file );
//        }
        else
        {
            throw new IOException( "Unable to resolve file type: " + file.getAbsolutePath() );
        }
    }
}
