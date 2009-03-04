package org.codehaus.mojo.unix.util.line;

import java.io.File;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface LineStreamWriter
{
    LineStreamWriter add();

    LineStreamWriter add( String line );

    LineStreamWriter addIfNotEmpty( String field, String value );

    LineStreamWriter addIfNotNull( File file );

    LineStreamWriter addIf( boolean flag, String value );

    LineStreamWriter addAllLines( Iterator<String> lines );

    LineStreamWriter addAllLines( Iterable<String> lines );
}
