import org.codehaus.mojo.unix.FileAttributes
import org.codehaus.mojo.unix.UnixFileMode
import org.codehaus.mojo.unix.UnixFsObject
import org.codehaus.mojo.unix.dpkg.Dpkg
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertDpkgEntries
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertFormat
import static org.codehaus.mojo.unix.maven.ShittyUtil.r

boolean success = true

assertFormat "DPKG", "dpkg", Dpkg.available(), {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-dpkg-2/1.1-2/project-dpkg-2-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(null, null, UnixFileMode.fromString("rwxr-xr-x"));
  FileAttributes hudsonWarAttributes = new FileAttributes(null, null, UnixFileMode.fromString("rw-r--r--"));

  success &= assertDpkgEntries(deb, [
          new UnixFsObject.DirectoryUnixOFsbject(r("."), null, dirAttributes),
          new UnixFsObject.DirectoryUnixOFsbject(r("usr"), null, dirAttributes),
          new UnixFsObject.DirectoryUnixOFsbject(r("usr/share"), null, dirAttributes),
          new UnixFsObject.DirectoryUnixOFsbject(r("usr/share/hudson"), null, dirAttributes),
          new UnixFsObject.DirectoryUnixOFsbject(r("usr/share/hudson/lib"), null, dirAttributes),
          new UnixFsObject.FileUnixOFsbject(r("usr/share/hudson/lib/slave.jar"), null, 158615, hudsonWarAttributes),
          new UnixFsObject.DirectoryUnixOFsbject(r("usr/share/hudson/license"), null, dirAttributes),
          new UnixFsObject.FileUnixOFsbject(r("usr/share/hudson/license/atom-license.txt"), null, 49, hudsonWarAttributes),
          new UnixFsObject.FileUnixOFsbject(r("usr/share/hudson/license/dc-license.txt"), null, 1544, hudsonWarAttributes),
  ])
}

return success
