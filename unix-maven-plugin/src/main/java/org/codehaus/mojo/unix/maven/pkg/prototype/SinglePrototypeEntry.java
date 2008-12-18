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

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SinglePrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public abstract class SinglePrototypeEntry
    extends AbstractPrototypeEntry
{
    private String path;

    private File realPath;

    protected SinglePrototypeEntry()
    {
    }

    protected SinglePrototypeEntry( String pkgClass, String mode, String user, String group, Boolean relative,
                                    String path, File realPath )
    {
        super( pkgClass, mode, user, group, relative );
        this.path = path;
        this.realPath = realPath;
    }

    public String getPath()
    {
        if ( isRelative() != null && isRelative().booleanValue() )
        {
            if ( path.charAt( 0 ) != '/' )
            {
                return path;
            }

            return path.substring( 1 );
        }
        else
        {
            if ( path.charAt( 0 ) == '/' )
            {
                return path;
            }

            return "/" + path;
        }
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public File getRealPath()
    {
        return realPath;
    }

    public void setRealPath( File realPath )
    {
        this.realPath = realPath;
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

//    public void validate( Defaults defaults )
//    {
//        super.validate( defaults );
//
//        if ( path == null )
//        {
//            throw new RuntimeException( "Missing path in directory entry." );
//        }
//    }

    public abstract String generatePrototypeLine();
}
