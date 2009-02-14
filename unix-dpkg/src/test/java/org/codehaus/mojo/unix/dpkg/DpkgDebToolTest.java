package org.codehaus.mojo.unix.dpkg;

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
        List list = DpkgDebTool.contents( getTestFile( "src/test/resources/base-files_4_i386.deb" ) );

        assertEquals( 66, list.size() );
        assertEquals( UnixFsObject.DirectoryUnixOFsbject.class, list.get( 0 ).getClass() );
        int i = 0;
        assertEquals( RelativePath.BASE, ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/FAQ" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/README.FHS" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/README.base" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/copyright" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/remove-base" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/doc/base-files/changelog.gz" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/dot.bashrc" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/dot.profile" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/info.dir" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/motd.md5sums" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/nsswitch.conf" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/profile" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/base-files/motd" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/Artistic" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/BSD" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/GPL-2" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/LGPL-2" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/LGPL-2.1" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/dict/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/info/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/man/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/misc/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/bin/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/games/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/include/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/lib/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/sbin/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/src/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "bin/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "boot/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "dev/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/default/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/skel/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/debian_version" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/host.conf" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/issue" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "etc/issue.net" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "home/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "lib/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "mnt/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "proc/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "root/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "sbin/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "tmp/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/backups/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/cache/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/lib/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/lib/dpkg/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/lib/misc/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/local/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/lock/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/log/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/run/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/spool/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "var/tmp/" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/LGPL" ), ( (UnixFsObject) list.get( i++ ) ).path );
        assertEquals( RelativePath.fromString( "usr/share/common-licenses/GPL" ), ( (UnixFsObject) list.get( i++ ) ).path );
    }
}
