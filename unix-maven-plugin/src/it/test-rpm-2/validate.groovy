import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.SpecFile

boolean success = true

File rpm = findArtifact("bar", "project-rpm-2", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "hudson", "hudson", "-r--r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "nobody", "nobody", "-r--r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "nobody", "nobody", "-r--r--r--", 1544, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-2"
specFile.version = "1.1"
specFile.release = 2
specFile.summary = "Unnamed - bar:project-rpm-2:rpm:1.1-2"
specFile.license = "BSD"
specFile.group = "Application/Collectors"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(rpm), specFileEqual);

return success
