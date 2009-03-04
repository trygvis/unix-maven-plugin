import static fj.data.Option.none
import static fj.data.Option.some
import org.codehaus.mojo.unix.FileAttributes
import static org.codehaus.mojo.unix.UnixFileMode.fromString
import org.codehaus.mojo.unix.UnixFsObject
import static org.codehaus.mojo.unix.UnixFsObject.directory
import static org.codehaus.mojo.unix.UnixFsObject.regularFile
import org.codehaus.mojo.unix.dpkg.Dpkg
import static org.codehaus.mojo.unix.maven.ShittyUtil.START_OF_TIME
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertDpkgEntries
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertFormat
import static org.codehaus.mojo.unix.maven.ShittyUtil.r

boolean success = true

assertFormat "DPKG", "dpkg", Dpkg.available(), {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-dpkg-1/1.1-2/project-dpkg-1-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(none(), none(), some(fromString("rwxr-xr-x")));
  FileAttributes hudsonWarAttributes = new FileAttributes(none(), none(), some(fromString("rw-r--r--")));

  // Ignore dates for now
  success &= assertDpkgEntries(deb, (List<UnixFsObject>)[
          directory(r("."), none(), dirAttributes),
          directory(r("usr",), none(), dirAttributes),
          directory(r("usr/share",), none(), dirAttributes),
          directory(r("usr/share/hudson",), none(), dirAttributes),
          directory(r("usr/share/hudson/lib",), none(), dirAttributes),
          regularFile(r("usr/share/hudson/lib/hudson.war"), START_OF_TIME, 20623413, some(hudsonWarAttributes)),
  ])
}

return success
