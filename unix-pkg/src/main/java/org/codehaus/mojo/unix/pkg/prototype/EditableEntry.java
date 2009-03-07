package org.codehaus.mojo.unix.pkg.prototype;

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

import fj.data.Option;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: EditableEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class EditableEntry
    extends PrototypeEntry
{
    private final Option<File> realPath;

    private final FileAttributes attributes;

    public EditableEntry( Option<String> pkgClass, Option<Boolean> relative, RelativePath path, Option<File> realPath,
                          FileAttributes attributes )
    {
        super( pkgClass, relative, path );
        this.realPath = realPath;
        this.attributes = attributes;
    }

    public String generatePrototypeLine()
    {
        return "e " + pkgClass +
            " " + getProcessedPath( realPath ) +
            " " + toString( attributes );
    }
}
