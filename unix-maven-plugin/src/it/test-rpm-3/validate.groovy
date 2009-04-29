import static org.codehaus.mojo.unix.maven.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil

boolean success = true

File main = findArtifact("bar", "project-rpm-3", "1.1-2", "rpm")

println "************************************************************************"
println "Hudson Master"

success &= assertRpmEntries(main, [
        new RpmUtil.FileInfo("/usr", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "root", "bin", "-rw-r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "root", "bin", "-rw-r--r--", 1544, null),
])

println "************************************************************************"
println "Hudson Slave"

File slave = findArtifact("bar", "project-rpm-3", "1.1-2", "rpm", "slave")
success &= assertRpmEntries(slave, [
        new RpmUtil.FileInfo("/usr", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share","nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "nobody", "nogroup", "-rw-r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "root", "bin", "-rw-r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "root", "bin", "-rw-r--r--", 1544, null),
])

return success
