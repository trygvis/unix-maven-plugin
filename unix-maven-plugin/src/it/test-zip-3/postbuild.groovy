import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.FileAttributes.*
import static org.codehaus.mojo.unix.UnixFsObject.*
import static fj.data.Option.*

boolean success = true

File main = findArtifact("bar", "project-zip-3", "1.1-2", "zip")

println "************************************************************************"
println "Hudson Master"

success &= assertZipEntries(main, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/LICENSE-downstream.txt"), START_OF_TIME, 15, some(EMPTY)),
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/hudson.war"), START_OF_TIME, 20623413, some(EMPTY)),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), START_OF_TIME, 49, some(EMPTY)),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), START_OF_TIME, 1544, some(EMPTY)),
        directory(r("usr/share/hudson/server"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/server/README.txt"), START_OF_TIME, 35, some(EMPTY)),
])

println "************************************************************************"
println "Hudson Slave"

File slave = findArtifact("bar", "project-zip-3", "1.1-2", "zip", "slave")
success &= assertZipEntries(slave, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/LICENSE-downstream.txt"), START_OF_TIME, 15, some(EMPTY)),
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/slave.jar"), START_OF_TIME, 158615, some(EMPTY)),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), START_OF_TIME, 49, some(EMPTY)),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), START_OF_TIME, 1544, some(EMPTY)),
        directory(r("usr/share/hudson/slave"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/slave/README.txt"), START_OF_TIME, 34, some(EMPTY)),
])


return success
