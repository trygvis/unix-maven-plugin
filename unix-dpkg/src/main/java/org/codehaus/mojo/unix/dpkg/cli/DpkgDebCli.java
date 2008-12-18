package org.codehaus.mojo.unix.dpkg.cli;

import org.codehaus.mojo.unix.ar.Ar;
import org.codehaus.mojo.unix.ar.ArUtil;
import org.codehaus.mojo.unix.ar.CloseableIterable;
import org.codehaus.mojo.unix.ar.ReadableArFile;
import org.codehaus.plexus.archiver.tar.TarEntry;
import org.codehaus.plexus.archiver.tar.TarInputStream;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgDebCli
{
    private PrintWriter out;

    public DpkgDebCli( PrintWriter out )
    {
        this.out = out;
    }

    public void main( String[] args )
        throws Exception
    {
        String command = args[0];

        if ( command.equals( "-c" ) )
        {
            contents( new File( args[1] ) );
        }
    }

    public void contents( File debFile )
        throws IOException
    {
        CloseableIterable reader = null;

        try
        {
            reader = Ar.read( debFile );
            for ( Iterator it = reader.iterator(); it.hasNext(); )
            {
                ReadableArFile file = (ReadableArFile) it.next();

                if ( file.getName().equals( "data.tar.gz" ) )
                {
                    dumpContents( file );
                    break;
                }
            }
        }
        finally
        {
            ArUtil.close( reader );
        }
    }

    private void dumpContents( ReadableArFile arFile )
        throws IOException
    {
        TarInputStream tis = null;
        try
        {
            tis = new TarInputStream( new GZIPInputStream( arFile.open() ) );

            TarEntry entry = tis.getNextEntry();
            while ( entry != null )
            {
                print( entry );
                entry = tis.getNextEntry();
            }
        }
        finally
        {
            IOUtil.close( tis );
        }
    }

    final SimpleDateFormat dateFormat;

    {
        dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    private void print( TarEntry entry )
    {
        String type;

        if ( !entry.getLinkName().equals( "" ) )
        {
            type = "l";
        }
        else if ( entry.isDirectory() )
        {
            type = "d";
        }
        else
        {
            type = "-";
        }

        String mode = mode( entry.getMode() );
        String ug = entry.getUserName() + "/" + entry.getGroupName();
        String date = dateFormat.format( entry.getModTime() );
        String size = StringUtils.leftPad( Long.toString( entry.getSize() ), 9 );
        out.print( type + mode + " " + ug + " " + size + " " + date + " " + entry.getName() );

        if ( type.equals( "l" ) )
        {
            out.print( " -> " + entry.getLinkName() );
        }
        out.println();
    }

    private String mode( int mode )
    {
        StringBuffer builder = new StringBuffer( "         " );
        builder.setLength( 9 );
        builder.setCharAt( 0, ( mode & 0x100 ) > 0 ? 'r' : '-' );
        builder.setCharAt( 1, ( mode & 0x080 ) > 0 ? 'w' : '-' );
        builder.setCharAt( 2, ( mode & 0x040 ) > 0 ? 'x' : '-' );
        builder.setCharAt( 3, ( mode & 0x020 ) > 0 ? 'r' : '-' );
        builder.setCharAt( 4, ( mode & 0x010 ) > 0 ? 'w' : '-' );
        builder.setCharAt( 5, ( mode & 0x400 ) > 0 ? 's' : ( mode & 0x008 ) > 0 ? 'x' : '-' );
        builder.setCharAt( 6, ( mode & 0x004 ) > 0 ? 'r' : '-' );
        builder.setCharAt( 7, ( mode & 0x002 ) > 0 ? 'w' : '-' );
        builder.setCharAt( 8, ( mode & 0x001 ) > 0 ? 'x' : '-' );

        return builder.toString();
    }
}
