import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.SpecFile

boolean success = true

File main = findArtifact("bar", "project-rpm-3", "1.1-2", "rpm")

println "************************************************************************"
println "Hudson Master"

success &= assertRpmEntries(main, [
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "root", "bin", "-rw-r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "root", "bin", "-rw-r--r--", 1544, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-3"
specFile.version = "1.1"
specFile.release = 2
specFile.summary = "Hudson Master"
specFile.license = "BSD"
specFile.group = "Application/Collectors"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(main), specFileEqual);

println "************************************************************************"
println "Hudson Slave"

File slave = findArtifact("bar", "project-rpm-3", "1.1-2", "rpm", "slave")
success &= assertRpmEntries(slave, [
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share","root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "root", "root", "-rw-r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "root", "bin", "-rw-r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "root", "bin", "-rw-r--r--", 1544, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-3-slave"
specFile.version = "1.1"
specFile.release = 2
specFile.summary = "Hudson Slave"
specFile.license = "BSD"
specFile.group = "Application/Collectors"
specFile.description = "Hudson slave node"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(slave), specFileEqual);

return success
