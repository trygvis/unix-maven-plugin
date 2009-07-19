package org.codehaus.mojo.unix.maven.logging;

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

import org.apache.commons.logging.*;
import org.apache.commons.logging.impl.*;

public class MavenCommonLoggingLogFactory
    extends org.apache.commons.logging.LogFactory
{
    private static final ThreadLocal<org.apache.maven.plugin.logging.Log> mavenLogger =
        new ThreadLocal<org.apache.maven.plugin.logging.Log>();

    public Object getAttribute( String s )
    {
        return null;
    }

    public String[] getAttributeNames()
    {
        return null;
    }

    public Log getInstance( Class aClass )
        throws LogConfigurationException
    {
        org.apache.maven.plugin.logging.Log log = mavenLogger.get();

        if ( log == null )
        {
            return new NoOpLog(); // This *should* only happen during testing -- famous last words - trygvis
//            throw new RuntimeException( "INTERNAL ERROR: maven logger is null." );
        }

        return new CommonsLogAdapter( log );
    }

    public Log getInstance( String s )
        throws LogConfigurationException
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void release()
    {
    }

    public void removeAttribute( String s )
    {
    }

    public void setAttribute( String s, Object o )
    {
    }

    public static void setMavenLogger( org.apache.maven.plugin.logging.Log mavenLogger )
    {
        MavenCommonLoggingLogFactory.mavenLogger.set( mavenLogger );
    }
}
