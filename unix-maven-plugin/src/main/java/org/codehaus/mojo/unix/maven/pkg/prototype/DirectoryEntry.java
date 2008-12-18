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
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: DirectoryEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class DirectoryEntry
    extends SinglePrototypeEntry
{
    public DirectoryEntry()
    {
    }

    public DirectoryEntry( String pkgClass, String mode, String user, String group, Boolean relative,
                           String path, File realPath )
    {
        super( pkgClass, mode, user, group, relative, path, realPath );
    }

    public String generatePrototypeLine()
    {
        return "d " + getPkgClass() + " " + getPath() + " " + getMode() + " " + getUser() + " " + getGroup();
    }
}
