import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.FileAttributes.*
import static org.codehaus.mojo.unix.UnixFsObject.*
import static fj.data.Option.*
import org.joda.time.*;

boolean success = true

File main = findArtifact("bar", "project-zip-3", "1.1-2", "zip")

ts1 = new LocalDateTime(2008, 10, 2, 2, 7, 36)
ts2 = new LocalDateTime(2011, 9, 23, 17, 30, 8)

licenseDownstream = regularFile(r("/usr/share/hudson/LICENSE-downstream.txt"), ts2, 15, EMPTY)

println "************************************************************************"
println "Hudson Master"

success &= assertZipEntries(main, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        licenseDownstream,
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/hudson.war"), hudsonWarTimestamp, 20623413, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), ts1, 49, EMPTY),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), ts1, 1544, EMPTY),
        directory(r("usr/share/hudson/server"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/server/README.txt"), ts2, 35, EMPTY),
])

println "************************************************************************"
println "Hudson Slave"

File slave = findArtifact("bar", "project-zip-3", "1.1-2", "zip", "slave")
success &= assertZipEntries(slave, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        licenseDownstream,
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/slave.jar"), ts1, 158615, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), ts1, 49, EMPTY),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), ts1, 1544, EMPTY),
        directory(r("usr/share/hudson/slave"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/slave/README.txt"), ts2, 34, EMPTY),
])


return success
