import static org.codehaus.mojo.unix.maven.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil

boolean success = true

File rpm = findArtifact("bar", "project-rpm-2", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/usr", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "hudson", "hudson", "-r--r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "nobody", "nobody", "-r--r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "nobody", "nobody", "-r--r--r--", 1544, null),
])

return success
