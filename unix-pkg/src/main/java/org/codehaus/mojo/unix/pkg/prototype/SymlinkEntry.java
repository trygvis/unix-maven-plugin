package org.codehaus.mojo.unix.pkg.prototype;

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

import fj.data.Option;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.UnixUtil.noneBoolean;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: AbstractPrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class SymlinkEntry
    extends PrototypeEntry
{
    private String to;

    protected SymlinkEntry( Option<String> pkgClass, RelativePath path, String to )
    {
        super( pkgClass, noneBoolean, path );
        validateNotNull( to );

        this.to = to;
    }

    public String generatePrototypeLine()
    {
        return "s " + pkgClass + " " + getPath() + "=" + to;
    }
}
