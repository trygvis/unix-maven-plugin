import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.FileAttributes.*
import static org.codehaus.mojo.unix.UnixFsObject.*
import static fj.data.Option.*;
import org.joda.time.*;

boolean success = true

File zip = findArtifact("bar", "project-zip-1", "1.1-2", "zip")

success &= assertZipEntries(zip, [
        directory(r("/opt"), START_OF_TIME, EMPTY),
        directory(r("/opt/hudson"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/hudson.war"), hudsonWarTimestamp, 20623413, EMPTY),

        directory(r("/opt/hudson/etc"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/etc/config.properties"), new LocalDateTime(2012, 8, 23, 12, 37, 12), 14, EMPTY),
])

return success
