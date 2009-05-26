import static org.codehaus.mojo.unix.maven.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.RpmUtil.SpecFile

boolean success = true

File hudsonWar = findArtifact("org.jvnet.hudson.main", "hudson-war", "1.255", "war")
// Set this as the rpm command will spit out dates in a different format than what the RpmUtil can handle right now
hudsonWar.setLastModified(System.currentTimeMillis())

File rpm = findArtifact("bar", "project-rpm-1", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/opt", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/usr", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/README.txt", "nobody", "nogroup", "-rw-r--r--", 38, null),
        new RpmUtil.FileInfo("/var", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/var/log", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        // TODO: This should assert the target
        new RpmUtil.FileInfo("/var/log/hudson", "nobody", "nogroup", "lrwxrwxrwx", 19, null),
])

success &= assertRelaxed(
        new SpecFile( "project-rpm-1", "1.1", 2, "Unnamed - bar:project-rpm-1:rpm:1.1-2", "BSD", "Application/Collectors", "", []),
        RpmUtil.getSpecFileFromRpm(rpm));

return success
