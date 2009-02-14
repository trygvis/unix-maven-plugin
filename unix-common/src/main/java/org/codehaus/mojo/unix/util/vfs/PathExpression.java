package org.codehaus.mojo.unix.util.vfs;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PathExpression
{
    private static final String REGEX_CHARS = "\\+()^$.{}]|";

    private final String expression;

    private Pattern pattern;

    public PathExpression( String expression )
    {
        this.expression = expression;
    }

    public String getExpression()
    {
        return expression;
    }

    public Pattern getPattern()
    {
        if ( pattern != null )
        {
            return pattern;
        }

        StringBuffer buffer = new StringBuffer();

        // TODO: it might be required to have a slash on the start here as that is always inserted on
        // the path to be matched
        buffer.append( "^" );

        // Make sure the expression start with a slash unless the expression start with a **
        if ( !expression.startsWith( "**" ) && !expression.startsWith( "/" ) )
        {
            buffer.append( '/' );
        }

        for ( int i = 0; i < expression.length(); i++ )
        {
            char c = expression.charAt( i );

            if ( c == '*' )
            {
                if ( expression.length() > ( i + 1 ) && expression.charAt( i + 1 ) == '*' )
                {
                    buffer.append( ".*" );
                    i++;
                }
                else
                {
                    buffer.append( "[^/]*" );
                }
            }
            else if ( c == '?' )
            {
                buffer.append( "." );
            }
            else if ( REGEX_CHARS.indexOf( c ) != -1 )
            {
                buffer.append( '\\' ).append( c );
            }
            else
            {
                buffer.append( c );
            }
        }

        buffer.append( "$" );

        return pattern = Pattern.compile( buffer.toString() );
    }

    public boolean matches( String relative )
    {
        return getPattern().matcher( relative ).matches();
    }
}
