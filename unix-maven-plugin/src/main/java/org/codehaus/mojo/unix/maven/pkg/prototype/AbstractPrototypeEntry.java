package org.codehaus.mojo.unix.maven.pkg.prototype;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: AbstractPrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public abstract class AbstractPrototypeEntry
{
    static final String EOL = System.getProperty( "line.separator" );

    private String pkgClass;

    private String mode;

    private String user;

    private String group;

    private Boolean relative;

    protected AbstractPrototypeEntry()
    {
    }

    protected AbstractPrototypeEntry( String pkgClass, String mode, String user, String group, Boolean relative )
    {
        this.pkgClass = pkgClass;
        this.mode = mode;
        this.user = user;
        this.group = group;
        this.relative = relative;
    }

    public String getPkgClass()
    {
        return pkgClass == null ? "none" : pkgClass;
    }

    public void setPkgClass( String pkgClass )
    {
        this.pkgClass = pkgClass;
    }

    public void setClass( String clazz )
    {
        setPkgClass( clazz );
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode( String mode )
    {
        this.mode = mode;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public Boolean isRelative()
    {
        return relative;
    }

    public void setRelative( Boolean relative )
    {
        this.relative = relative;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

//    public void validate( Defaults defaults )
//    {
//        if ( pkgClass == null )
//        {
//            pkgClass = defaults.getPkgClass();
//        }
//
//        if ( mode == null )
//        {
//            mode = defaults.getMode();
//        }
//
//        if ( user == null )
//        {
//            user = defaults.getUser();
//        }
//
//        if ( group == null )
//        {
//            group = defaults.getGroup();
//        }
//
//        if ( relative == null )
//        {
//            relative = defaults.isRelative();
//        }
//    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
    }

    public static AbstractPrototypeEntry fromLine( String line )
    {
        String[] parts = line.split( " " );

        if ( parts.length < 6 )
        {
            throw new RuntimeException( "Invalid line, expected at least 6 parts." );
        }

        String type = parts[0];

        if ( "f".equals( type ) )
        {
            if ( parts.length == 7 )
            {
                return new FileEntry( parts[1], parts[2], parts[3], parts[4], Boolean.FALSE, parts[6], null );
            }
            else
            {
                return new FileEntry( parts[1], parts[2], parts[3], parts[4], Boolean.FALSE, null, null );
            }
        }
        else
        {
            throw new RuntimeException( "Unknown file type '" + type + "'." );
        }
    }
}
