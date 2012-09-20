package org.codehaus.mojo.unix.maven.plugin;

import org.joda.time.*;

import java.io.*;
import java.lang.reflect.*;

import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*;

public class Timestamps
{
    private final File basedir;

    class Common
    {
        public final FileTimestamp hudsonWarTimestamp =
            new FileTimestamp( findArtifact( "org.jvnet.hudson.main", "hudson-war", "1.255", "war" ),
                               new LocalDateTime( 2010, 9, 30, 10, 12, 56 ) );

        public void setTimestamps()
            throws Exception
        {
            setTimestamps( getClass() );
        }

        public void setTimestamps(Class klass)
            throws Exception
        {
            for ( Field field : klass.getDeclaredFields() )
            {
                if ( !field.getType().isAssignableFrom( FileTimestamp.class ) )
                {
                    continue;
                }

                FileTimestamp ft = (FileTimestamp) field.get( this );

                if ( !ft.file.canRead() )
                {
                    throw new IOException( "No such file: " + ft.file );
                }

                if ( !ft.file.setLastModified( ft.timestamp.toDateTime().getMillis() ) )
                {
                    throw new IOException( "Unable to set ft." );
                }
            }

            if(klass != Object.class) {
                setTimestamps( klass.getSuperclass() );
            }
        }
    }

    class Zip1
        extends Common
    {
        public final FileTimestamp configProperties =
            ft( new File( basedir, "src/main/unix/files/opt/hudson/etc/config.properties" ), 2012, 1, 2, 3, 4, 6 );
    }

    public final Zip1 zip1;

    public Timestamps( File basedir )
    {
        this.basedir = basedir;
        zip1 = new Zip1();
    }

    public class FileTimestamp
    {
        public final File file;

        public final LocalDateTime timestamp;

        public FileTimestamp( File file, LocalDateTime timestamp )
        {
            this.file = file;
            this.timestamp = timestamp;
        }
    }

    public FileTimestamp ft( File file, int y, int m, int d, int h, int min, int s )
    {
        return new FileTimestamp( file, new LocalDateTime( y, m, d, h, min, s ) );
    }
}
