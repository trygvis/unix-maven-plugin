import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.rpm.RpmUtil

boolean success = true

File rpm = new File((File) basedir, "target/project-rpm-2-1.1-2.rpm")
success &= ShittyUtil.assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "hudson", "hudson", "-r--r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "nobody", "nobody", "-r--r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "nobody", "nobody", "-r--r--r--", 1544, null),
])

return success
