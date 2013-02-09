import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.FileAttributes.*
import static org.codehaus.mojo.unix.UnixFsObject.*
import org.joda.time.*;

boolean success = true

File zip = findArtifact("bar", "project-zip-2", "1.1-2", "zip")

ts = new LocalDateTime(2008, 10, 2, 2, 7, 36)

success &= assertZipEntries(zip, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/slave.jar"), ts, 158615, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), ts, 49, EMPTY),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), ts, 1544, EMPTY),
])
return success
