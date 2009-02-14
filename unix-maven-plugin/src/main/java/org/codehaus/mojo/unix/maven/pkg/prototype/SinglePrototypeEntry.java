package org.codehaus.mojo.unix.maven.pkg.prototype;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: SinglePrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public abstract class SinglePrototypeEntry
    extends PrototypeEntry
{
    static final String EOL = System.getProperty( "line.separator" );

    private final String pkgClass;

    private final UnixFileMode mode;

    private final String user;

    private final String group;

    private final Boolean relative;

    private final RelativePath path;

    private final File realPath;

    protected SinglePrototypeEntry( String pkgClass, UnixFileMode mode, String user, String group, Boolean relative,
                                    RelativePath path, File realPath )
    {
        this.pkgClass = pkgClass == null ? "none" : pkgClass;
        this.mode = mode;
        this.user = user;
        this.group = group;
        this.relative = relative;
        this.path = path;
        this.realPath = realPath;
    }

    public String getPkgClass()
    {
        return pkgClass;
    }

    public UnixFileMode getMode()
    {
        return mode;
    }

    public String getUser()
    {
        return user;
    }

    public String getGroup()
    {
        return group;
    }

    public Boolean isRelative()
    {
        return relative;
    }

    public File getRealPath()
    {
        return realPath;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public String toString()
    {
        return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
    }

    public String getPath()
    {
        if ( isRelative() != null && isRelative().booleanValue() )
        {
            return path.string;
        }

        return path.asAbsolutePath();
    }

    public String getModeString()
    {
        return mode != null ? mode.toOctalString() : "?";
    }

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || SinglePrototypeEntry.class != o.getClass() )
        {
            return false;
        }

        SinglePrototypeEntry that = (SinglePrototypeEntry) o;

        return path.equals( that.path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected String getProcessedPath()
    {
        if ( realPath == null )
        {
            return getPath();
        }
        else
        {
            return getPath() + "=" + realPath.getAbsolutePath();
        }
    }

    public final String getPrototypeLine()
    {
        return generatePrototypeLine();
    }
}
