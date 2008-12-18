package org.codehaus.mojo.unix.dpkg;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.mojo.unix.util.SystemCommand;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Builds a DEB package using fakeroot dpkg-deb.  This command creates a DEB
 * package in buildDir using the contents of stageDir.  This classes
 * assumes that stageDir is going to be a direct descendent of buildDir.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: Dpkg.java 7127 2008-06-05 08:51:58Z trygvis $
 */
public class Dpkg
{
    private boolean debug;

    private File packageRoot;

    private File debFile;

    private boolean useFakeroot;

    private String dpkgDebPath = "dpkg-deb";

    public Dpkg setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public Dpkg setPackageRoot( File packageRoot )
    {
        this.packageRoot = packageRoot;
        return this;
    }

    public Dpkg setDebFile( File debFile )
    {
        this.debFile = debFile;
        return this;
    }

    public Dpkg setUseFakeroot( boolean useFakeroot )
    {
        this.useFakeroot = useFakeroot;
        return this;
    }

    public Dpkg setDpkgDebPath( String dpkgDebPath )
    {
        this.dpkgDebPath = StringUtils.defaultString( dpkgDebPath, this.dpkgDebPath );
        return this;
    }

    public void execute()
        throws IOException
    {
        if ( packageRoot == null )
        {
            throw new IOException( "Package root is not set." );
        }

        if ( debFile == null )
        {
            throw new IOException( "Path to output .deb is not set." );
        }

        new SystemCommand().
            setCommand( useFakeroot ? "fakeroot" : dpkgDebPath ).
            dumpCommandIf( debug ).
            dumpOutputIf( debug ).
            addArgument( "-b" ).
            addArgumentIf( useFakeroot, dpkgDebPath ).
            addArgument( packageRoot.getAbsolutePath() ).
            addArgument( debFile.getAbsolutePath() ).
            execute().
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "dpkg" );
    }
}
