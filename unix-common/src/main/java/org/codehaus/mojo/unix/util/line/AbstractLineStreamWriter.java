package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Iterator;

public abstract class AbstractLineStreamWriter
    implements LineStreamWriter
{
    public static final String EOL = System.getProperty( "line.separator" );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected abstract void onLine( String line );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public LineStreamWriter add()
    {
        onLine( null );
        return this;
    }

    public LineStreamWriter add( String line )
    {
        onLine( line );
        return this;
    }

    public LineStreamWriter add( LineProducer lineProducer )
    {
        lineProducer.streamTo( this );
        return this;
    }

    public LineStreamWriter addIfNotEmpty( String field, String value )
    {
        if ( StringUtils.isNotEmpty( value ) )
        {
            onLine( value );
        }
        return this;
    }

    public LineStreamWriter addIfNotNull( File file )
    {
        if ( file != null )
        {
            onLine( file.getName() );
        }
        return this;
    }

    public LineStreamWriter addIf( boolean flag, String value )
    {
        if ( flag )
        {
            onLine( value );
        }
        return this;
    }

    public LineStreamWriter addAllLines( Iterator<String> lines )
    {
        if ( lines != null )
        {
            while ( lines.hasNext() )
            {
                add( (String) ((Iterator) lines).next() );
            }
        }
        return this;
    }

    public LineStreamWriter addAllLines( Iterable<String> lines )
    {
        if ( lines != null )
        {
            return addAllLines( lines.iterator() );
        }
        return this;
    }
}
