package org.codehaus.mojo.unix.util;

import org.codehaus.mojo.unix.MissingSettingException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnixUtil
{
    public static void assertField( String field, Object value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }
    }

    public static String getField( String field, String value )
        throws MissingSettingException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static File getField( String field, File value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static String getFieldOrDefault( String value, String defaultValue )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            return defaultValue;
        }

        return value;
    }

    public static void chmodIf( boolean b, File file, String mode )
        throws IOException
    {
        if ( b )
        {
            chmod( file, mode );
        }
    }

    public static void chmod( File file, String mode )
        throws IOException
    {
        new SystemCommand().
            setCommand( "chmod" ).
            addArgument( mode ).
            addArgument( file.getAbsolutePath() ).
            execute().
            assertSuccess( "Error while running chmod on " + file );
    }
}
