package org.codehaus.mojo.unix.maven.plugin;

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

import org.apache.maven.execution.*;
import org.apache.maven.plugin.*;
import org.apache.maven.project.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractUnixMojo
    extends AbstractMojo
{
    /**
     * @parameter
     */
    protected AssemblyOp[] assembly;

    /**
     * @parameter
     */
    protected Defaults defaults = new Defaults();

    /**
     * Optional parameter to specify the name of the package. If not set it will use the artifact id of the Maven
     * project.
     *
     * @parameter
     */
    protected String name;

    /**
     * Optional parameter to specify the revision of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected String revision;

    /**
     * @parameter property="project.description"
     */
    protected String description;

    /**
     * @parameter
     */
    protected String contact;

    /**
     * @parameter
     */
    protected String contactEmail;

    // TODO: Is this really common? Or just common enough?
    /**
     * @parameter
     */
    protected String architecture;

    /**
     * @parameter property="maven.unix.debug"
     */
    protected boolean debug;

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    /**
     * @parameter property="project"
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter property="session"
     * @readonly
     */
    protected ThreadLocal<MavenSession> session = new ThreadLocal<MavenSession>();

    /**
     * @component role="org.codehaus.mojo.unix.core.UnixPlatform"
     */
    protected Map platforms;

    /**
     * @component
     */
    protected MavenProjectHelper mavenProjectHelper;
}
