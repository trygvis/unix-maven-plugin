package org.codehaus.mojo.unix.maven;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPackageAttachedMojo
    extends AbstractUnixMojo
{
    private final String formatType;

    protected AbstractPackageAttachedMojo( String formatType )
    {
        this.formatType = formatType;
    }

    protected abstract MojoHelper getMojoHelper();

    protected MojoHelper getMojoHelper( MojoHelper mojoHelper )
    {
        return mojoHelper.
            attachedMode();
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getMojoHelper().
            setMojoParameters( new PackagingMojoParameters(
                name,
                version,
                revision,
                description,
                contact,
                contactEmail,
                architecture,
                assembly,
                null ) ).
            setup(
                formats,
                formatType,
                snapshotTransformation,
                project,
                mavenProjectHelper,
                debug,
                defaults ).execute();
    }
}
