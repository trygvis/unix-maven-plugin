package org.codehaus.mojo.unix.ar;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArCli
{
    public static void main( String[] args )
        throws IOException
    {

        File file = new File( "/Users/trygvis/dev/org.codehaus.mojo/trunk/sandbox/deb-maven-plugin/bash_3.1dfsg-8_i386.deb" );

        CloseableIterable reader = null;
        try
        {
            reader = Ar.read( file );
            for ( Iterator it = reader.iterator(); it.hasNext(); )
            {
                ArFile arFile = (ArFile) it.next();
                System.out.println( "arFile.getName() = " + arFile.getName() );
                System.out.println( "arFile.getLastModified() = " + arFile.getLastModified() );
                System.out.println( "arFile.getOwnerId() = " + arFile.getOwnerId() );
                System.out.println( "arFile.getGroupId() = " + arFile.getGroupId() );
                System.out.println( "arFile.getMode() = " + arFile.getMode() );
                System.out.println( "arFile.getSize() = " + arFile.getSize() );
            }
        }
        finally
        {
            ArUtil.close( reader );
        }
    }
}
