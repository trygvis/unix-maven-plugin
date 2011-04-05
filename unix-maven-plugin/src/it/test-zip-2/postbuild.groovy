import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.FileAttributes.*
import static org.codehaus.mojo.unix.UnixFsObject.*
import static fj.data.Option.*;

boolean success = true

File zip = findArtifact("bar", "project-zip-2", "1.1-2", "zip")

success &= assertZipEntries(zip, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/slave.jar"), START_OF_TIME, 158615, some(EMPTY)),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), START_OF_TIME, 49, some(EMPTY)),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), START_OF_TIME, 1544, some(EMPTY)),
])
return success
