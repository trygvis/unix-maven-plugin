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

import org.apache.maven.plugin.logging.Log;

public class CommonsLogAdapter
    implements org.apache.commons.logging.Log
{
    private final Log log;

    public CommonsLogAdapter( Log log )
    {
        this.log = log;
    }

    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return log.isErrorEnabled();
    }

    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled();
    }

    public boolean isTraceEnabled()
    {
        return log.isDebugEnabled();
    }

    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled();
    }

    public void trace( Object o )
    {
        log.debug( String.valueOf( o ) );
    }

    public void trace( Object o, Throwable throwable )
    {
        log.debug( String.valueOf( o ), throwable );
    }

    public void debug( Object o )
    {
        log.debug( String.valueOf( o ) );
    }

    public void debug( Object o, Throwable throwable )
    {
        log.debug( String.valueOf( o ), throwable );
    }

    public void info( Object o )
    {
        log.info( String.valueOf( o ) );
    }

    public void info( Object o, Throwable throwable )
    {
        log.info( String.valueOf( o ), throwable );
    }

    public void warn( Object o )
    {
        log.warn( String.valueOf( o ) );
    }

    public void warn( Object o, Throwable throwable )
    {
        log.warn( String.valueOf( o ), throwable );
    }

    public void error( Object o )
    {
        log.error( String.valueOf( o ) );
    }

    public void error( Object o, Throwable throwable )
    {
        log.error( String.valueOf( o ), throwable );
    }

    public void fatal( Object o )
    {
        log.error( String.valueOf( o ) );
    }

    public void fatal( Object o, Throwable throwable )
    {
        log.error( String.valueOf( o ), throwable );
    }
}
