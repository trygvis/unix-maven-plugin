package org.codehaus.mojo.unix.util;

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

import fj.data.*;
import static fj.data.List.*;
import static fj.pre.Equal.*;
import junit.framework.*;
import static org.codehaus.mojo.unix.util.FileModulator.*;

/**
 */
public class FileModulatorTest
    extends TestCase
{
    public void testScripts()
    {
        List<String> modulated = modulatePath( "id", "format", "src/main/unix/scripts/postinstall" );

        assertTrue( listEqual( stringEqual ).eq( modulated, list( "src/main/unix/scripts/postinstall",
                                                                  "src/main/unix/scripts/postinstall-id",
                                                                  "src/main/unix/scripts/postinstall-format",
                                                                  "src/main/unix/scripts/postinstall-id-format" ) ) );
    }

    public void testFilesModulation()
    {
        List<String> modulated = modulatePath( "id", "format", "src/main/unix/files" );

        assertTrue( listEqual( stringEqual ).eq( modulated, list( "src/main/unix/files", "src/main/unix/files-id",
                                                                  "src/main/unix/files-format",
                                                                  "src/main/unix/files-id-format" ) ) );
    }
}
