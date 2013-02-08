import static fj.data.Option.none
import static fj.data.Option.some
import org.codehaus.mojo.unix.FileAttributes
import static org.codehaus.mojo.unix.UnixFileMode.fromString
import org.codehaus.mojo.unix.UnixFsObject
import static org.codehaus.mojo.unix.UnixFsObject.directory
import static org.codehaus.mojo.unix.UnixFsObject.regularFile
import static org.codehaus.mojo.unix.UnixFsObject.symlink
import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.START_OF_TIME
import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.assertDebEntries
import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.assertFormat
import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.r

boolean success = true

assertFormat "deb", "dpkg-deb", true, {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-deb-1/1.1-2/project-deb-1-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(none(), none(), some(fromString("rwxr-xr-x")));
  FileAttributes hudsonWarAttributes = new FileAttributes(none(), none(), some(fromString("rw-r--r--")));
  FileAttributes logAttributes = new FileAttributes(some("root"), some("root"), some(fromString("rw-r--r--")));

  // Ignore dates for now
  success &= assertDebEntries(deb, (List<UnixFsObject>)[
          directory(r("."), START_OF_TIME, dirAttributes),
          directory(r("opt",), START_OF_TIME, dirAttributes),
          directory(r("opt/hudson",), START_OF_TIME, dirAttributes),
          regularFile(r("opt/hudson/hudson.war"), START_OF_TIME, 20623413, hudsonWarAttributes),
          directory(r("var",), START_OF_TIME, dirAttributes),
          directory(r("var/log",), START_OF_TIME, dirAttributes),
          symlink(r("var/log/hudson"), START_OF_TIME, some("root"), some("root"), "/var/opt/hudson/log"),
  ])
}

return success
