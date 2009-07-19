import fj.data.Option
import static fj.data.Option.none
import static fj.data.Option.some
import org.codehaus.mojo.unix.FileAttributes
import static org.codehaus.mojo.unix.UnixFileMode.fromString
import org.codehaus.mojo.unix.UnixFsObject
import static org.codehaus.mojo.unix.UnixFsObject.directory
import static org.codehaus.mojo.unix.UnixFsObject.regularFile
import org.codehaus.mojo.unix.deb.Dpkg
import static org.codehaus.mojo.unix.maven.ShittyUtil.START_OF_TIME
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertDebEntries
import static org.codehaus.mojo.unix.maven.ShittyUtil.assertFormat
import static org.codehaus.mojo.unix.maven.ShittyUtil.r

boolean success = true

assertFormat "deb", "dpkg", Dpkg.available(), {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-deb-2/1.1-2/project-deb-2-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(none(), none(), some(fromString("rwxr-xr-x")));
  FileAttributes hudsonWarAttributes = new FileAttributes(none(), none(), some(fromString("rw-r--r--")));

  success &= assertDebEntries(deb, (List<UnixFsObject>)[
          directory(r("."), none(), dirAttributes),
          directory(r("usr"), none(), dirAttributes),
          directory(r("usr/share"), none(), dirAttributes),
          directory(r("usr/share/hudson"), none(), dirAttributes),
          directory(r("usr/share/hudson/lib"), none(), dirAttributes),
          regularFile(r("usr/share/hudson/lib/slave.jar"), START_OF_TIME, 158615, some(hudsonWarAttributes)),
          directory(r("usr/share/hudson/license"), none(), dirAttributes),
          regularFile(r("usr/share/hudson/license/atom-license.txt"), START_OF_TIME, 49, some(hudsonWarAttributes)),
          regularFile(r("usr/share/hudson/license/dc-license.txt"), START_OF_TIME, 1544, some(hudsonWarAttributes)),
  ])
}

return success
