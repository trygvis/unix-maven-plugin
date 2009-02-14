package org.codehaus.mojo.unix.maven.pkg.prototype;

import org.codehaus.mojo.unix.UnixFileMode;
import org.codehaus.mojo.unix.util.RelativePath;

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
public abstract class PrototypeEntry
{
    public abstract String generatePrototypeLine();

    public static PrototypeEntry fromLine( String line )
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
                return new FileEntry( parts[1], UnixFileMode.fromString( parts[2] ), parts[3], parts[4],
                    RelativePath.fromString(parts[6] ) );
            }
            else
            {
                return new FileEntry( parts[1], UnixFileMode.fromString( parts[2] ), parts[3], parts[4], null );
            }
        }
        else
        {
            throw new RuntimeException( "Unknown file type '" + type + "'." );
        }
    }
}
