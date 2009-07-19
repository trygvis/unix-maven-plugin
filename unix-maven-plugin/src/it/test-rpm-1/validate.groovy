import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.SpecFile

boolean success = true

File hudsonWar = findArtifact("org.jvnet.hudson.main", "hudson-war", "1.255", "war")
// Set this as the rpm command will spit out dates in a different format than what the RpmUtil can handle right now
hudsonWar.setLastModified(System.currentTimeMillis())

File rpm = findArtifact("bar", "project-rpm-1", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/opt", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/README.txt", "root", "root", "-rw-r--r--", 38, null),
        new RpmUtil.FileInfo("/var", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/var/log", "root", "root", "drwxr-xr-x", 0, null),
        // TODO: This should assert the target
        new RpmUtil.FileInfo("/var/log/hudson", "root", "root", "lrwxrwxrwx", 19, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-1"
specFile.version = "1.1"
specFile.release = 2
specFile.summary = "Unnamed - bar:project-rpm-1:rpm:1.1-2"
specFile.license = "BSD"
specFile.group = "Application/Collectors"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(rpm), specFileEqual);

return success
