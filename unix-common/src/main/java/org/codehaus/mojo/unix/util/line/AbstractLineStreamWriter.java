package org.codehaus.mojo.unix.util.line;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractLineStreamWriter
    implements LineStreamWriter
{
    public static final String EOL = System.getProperty( "line.separator" );

    private String prefix;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected abstract void onLine( String prefix, String line );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public LineStreamWriter add()
    {
        onLine( null, null );
        return this;
    }

    public LineStreamWriter add( String line )
    {
        onLine( prefix, line );
        return this;
    }

    public LineStreamWriter addIfNotEmpty( String field, String value )
    {
        if ( StringUtils.isNotEmpty( value ) )
        {
            onLine( prefix, value );
        }
        return this;
    }

    public LineStreamWriter addIfNotNull( File file )
    {
        if ( file != null )
        {
            onLine( prefix, file.getName() );
        }
        return this;
    }

    public LineStreamWriter addIf( boolean flag, String value )
    {
        if ( flag )
        {
            onLine( prefix, value );
        }
        return this;
    }

    public LineStreamWriter addAllLines( Collection lines )
    {
        if ( lines != null )
        {
            for ( Iterator it = lines.iterator(); it.hasNext(); )
            {
                add( (String) it.next() );
            }
        }
        return this;
    }

    public LineStreamWriter setPrefix( String prefix )
    {
        this.prefix = prefix;
        return this;
    }

    public LineStreamWriter clearPrefix()
    {
        this.prefix = null;
        return this;
    }
}
