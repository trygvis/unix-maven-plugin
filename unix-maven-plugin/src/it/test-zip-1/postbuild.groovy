import org.codehaus.mojo.unix.maven.plugin.Timestamps

import static org.codehaus.mojo.unix.FileAttributes.EMPTY
import static org.codehaus.mojo.unix.UnixFsObject.directory
import static org.codehaus.mojo.unix.UnixFsObject.regularFile
import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*

boolean success = true

def timestamps = new Timestamps(basedir).zip1

File zip = findArtifact("bar", "project-zip-1", "1.1-2", "zip")

success &= assertZipEntries(zip, [
        directory(r("/opt"), START_OF_TIME, EMPTY),
        directory(r("/opt/hudson"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/hudson.war"), timestamps.hudsonWarTimestamp.timestamp, 20623413, EMPTY),

        directory(r("/opt/hudson/etc"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-1.conf"), timestamps.filter1.timestamp, 237, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-2.conf"), timestamps.filter2.timestamp, 88, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-all.conf"), timestamps.filterAll.timestamp, 157, EMPTY),
        regularFile(r("/opt/hudson/etc/unfiltered.properties"), timestamps.unfiltered.timestamp, 27, EMPTY),
])

return success
