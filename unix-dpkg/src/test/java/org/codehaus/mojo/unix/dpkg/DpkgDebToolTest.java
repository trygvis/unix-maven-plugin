package org.codehaus.mojo.unix.dpkg;

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

import org.codehaus.mojo.unix.UnixFsObject;
import org.codehaus.mojo.unix.util.RelativePath;
import org.codehaus.plexus.PlexusTestCase;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgDebToolTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        List<UnixFsObject> contents = DpkgDebTool.contents( getTestFile( "src/test/resources/base-files_4_i386.deb" ) );

        assertEquals( paths.length, contents.size() );
        assertEquals( UnixFsObject.Directory.class, contents.get( 0 ).getClass() );
        int i = 0;

        for (UnixFsObject unixFsObject : contents)
        {
            assertEquals(paths[i++], unixFsObject.path);
        }
    }

    private static final RelativePath[] paths = new RelativePath[]
    {
        RelativePath.BASE,
        RelativePath.fromString("usr/"),
        RelativePath.fromString("usr/share/"),
        RelativePath.fromString("usr/share/doc/"),
        RelativePath.fromString("usr/share/doc/base-files/"),
        RelativePath.fromString("usr/share/doc/base-files/FAQ"),
        RelativePath.fromString("usr/share/doc/base-files/README.FHS"),
        RelativePath.fromString("usr/share/doc/base-files/README.base"),
        RelativePath.fromString("usr/share/doc/base-files/copyright"),
        RelativePath.fromString("usr/share/doc/base-files/remove-base"),
        RelativePath.fromString("usr/share/doc/base-files/changelog.gz"),
        RelativePath.fromString("usr/share/base-files/"),
        RelativePath.fromString("usr/share/base-files/dot.bashrc"),
        RelativePath.fromString("usr/share/base-files/dot.profile"),
        RelativePath.fromString("usr/share/base-files/info.dir"),
        RelativePath.fromString("usr/share/base-files/motd.md5sums"),
        RelativePath.fromString("usr/share/base-files/nsswitch.conf"),
        RelativePath.fromString("usr/share/base-files/profile"),
        RelativePath.fromString("usr/share/base-files/motd"),
        RelativePath.fromString("usr/share/common-licenses/"),
        RelativePath.fromString("usr/share/common-licenses/Artistic"),
        RelativePath.fromString("usr/share/common-licenses/BSD"),
        RelativePath.fromString("usr/share/common-licenses/GPL-2"),
        RelativePath.fromString("usr/share/common-licenses/LGPL-2"),
        RelativePath.fromString("usr/share/common-licenses/LGPL-2.1"),
        RelativePath.fromString("usr/share/dict/"),
        RelativePath.fromString("usr/share/info/"),
        RelativePath.fromString("usr/share/man/"),
        RelativePath.fromString("usr/share/misc/"),
        RelativePath.fromString("usr/bin/"),
        RelativePath.fromString("usr/games/"),
        RelativePath.fromString("usr/include/"),
        RelativePath.fromString("usr/lib/"),
        RelativePath.fromString("usr/sbin/"),
        RelativePath.fromString("usr/src/"),
        RelativePath.fromString("bin/"),
        RelativePath.fromString("boot/"),
        RelativePath.fromString("dev/"),
        RelativePath.fromString("etc/"),
        RelativePath.fromString("etc/default/"),
        RelativePath.fromString("etc/skel/"),
        RelativePath.fromString("etc/debian_version"),
        RelativePath.fromString("etc/host.conf"),
        RelativePath.fromString("etc/issue"),
        RelativePath.fromString("etc/issue.net"),
        RelativePath.fromString("home/"),
        RelativePath.fromString("lib/"),
        RelativePath.fromString("mnt/"),
        RelativePath.fromString("proc/"),
        RelativePath.fromString("root/"),
        RelativePath.fromString("sbin/"),
        RelativePath.fromString("tmp/"),
        RelativePath.fromString("var/"),
        RelativePath.fromString("var/backups/"),
        RelativePath.fromString("var/cache/"),
        RelativePath.fromString("var/lib/"),
        RelativePath.fromString("var/lib/dpkg/"),
        RelativePath.fromString("var/lib/misc/"),
        RelativePath.fromString("var/local/"),
        RelativePath.fromString("var/lock/"),
        RelativePath.fromString("var/log/"),
        RelativePath.fromString("var/run/"),
        RelativePath.fromString("var/spool/"),
        RelativePath.fromString("var/tmp/"),
        RelativePath.fromString("usr/share/common-licenses/LGPL"),
        RelativePath.fromString("usr/share/common-licenses/GPL"),
    };
}
