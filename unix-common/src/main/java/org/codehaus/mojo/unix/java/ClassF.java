package org.codehaus.mojo.unix.java;

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

import fj.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 */
public class ClassF
{
    public static <A, B> F<A, B> cast()
    {
        return new F<A, B>()
        {
            @SuppressWarnings( {"unchecked"} )
            public B f( A a )
            {
                return (B) a;
            }
        };
    }

    public static final F<java.lang.Class, Package> getPackage = new F<java.lang.Class, Package>()
    {
        public Package f( java.lang.Class _this )
        {
            return _this.getPackage();
        }
    };

    public static final F<java.lang.Class, java.lang.String> getName = new F<java.lang.Class, String>()
    {
        public String f( java.lang.Class _this )
        {
            return _this.getName();
        }
    };

    public static final F2<java.lang.Class, java.lang.Class, Boolean> isAssignableFrom = new F2<java.lang.Class, java.lang.Class, Boolean>()
    {
        public Boolean f( java.lang.Class _this, java.lang.Class cls )
        {
            return _this.isAssignableFrom( cls );
        }
    };
}
