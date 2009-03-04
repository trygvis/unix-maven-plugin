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
import static fj.data.Option.some;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.HasFileAttributes;
import static org.codehaus.mojo.unix.UnixFileMode.showOcalString;
import org.codehaus.mojo.unix.util.RelativePath;

import java.io.File;
import static java.lang.Boolean.FALSE;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: FileEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class FileEntry
    extends PrototypeEntry
    implements HasFileAttributes<FileEntry>
{
    private final Option<File> realPath;

    private final FileAttributes attributes;

    /**
     * The same as calling {@link #FileEntry(Option, RelativePath, Option, Option, FileAttributes)}
     * with <code>relative=false</code> and <code>realPath=null</code>.
     */
    public FileEntry( Option<String> pkgClass, RelativePath path, FileAttributes attributes )
    {
        this( pkgClass, path, some( FALSE ) , Option.<File>none(), attributes );
    }

    public FileEntry( Option<String> pkgClass, RelativePath path, Option<Boolean> relative, Option<File> realPath, FileAttributes attributes )
    {
        super( pkgClass, relative, path );
        this.realPath = realPath;
        this.attributes = attributes;
    }

    public FileEntry getAggregator()
    {
        return this;
    }

    public FileAttributes getFileAttributes()
    {
        return attributes;
    }

    public FileEntry setFileAttributes( FileAttributes attributes )
    {
        return new FileEntry( some( pkgClass ), path, relative, realPath, attributes );
    }

    public String generatePrototypeLine()
    {
        return "f " + pkgClass +
            " " + getProcessedPath( realPath ) +
            " " + toString( attributes );
    }

    public static String getModeString( FileAttributes attributes )
    {
        return attributes.mode.map( showOcalString ).orSome( "?" );
    }
}
