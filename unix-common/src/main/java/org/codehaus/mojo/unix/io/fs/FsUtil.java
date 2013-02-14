package org.codehaus.mojo.unix.io.fs;

import java.io.*;
import java.util.*;

public class FsUtil
{
    private final static String[] zipFileTypes = { "zip", "jar", "war", "ear", "sar", };

    static {
        Arrays.sort( zipFileTypes );
    }

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

        int i = file.getName().lastIndexOf( '.' );

        if ( i < 1 )
        {
            throw new IOException( "Unable to resolve file type of file: " + file.getAbsolutePath() );
        }

        String ending = file.getName().substring( i + 1 );

        if ( Arrays.binarySearch( zipFileTypes, ending ) >= 0 )
        {
            return new ZipFsRoot( file );
        }
        else
        {
            throw new IOException( "Unable to resolve file type of file: " + file.getAbsolutePath() );
        }
    }
}
