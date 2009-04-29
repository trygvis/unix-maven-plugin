package org.codehaus.mojo.unix.util.vfs;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.regex.*;

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
