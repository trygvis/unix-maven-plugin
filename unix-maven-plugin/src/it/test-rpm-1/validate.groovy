import static org.codehaus.mojo.unix.maven.ShittyUtil.*
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.RpmUtil.SpecFile

boolean success = true

File rpm = new File((File) basedir, "target/project-rpm-1-1.1-2.rpm")
success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/opt/hudson", "nobody", "nogroup", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/var/log/hudson", "nobody", "nogroup", "lrwxrwxrwx", 19, null),
])

success &= assertRelaxed(
        new SpecFile( "project-rpm-1", "1.1", 2, "Unnamed - bar:project-rpm-1:rpm:1.1-2", "BSD", "Application/Collectors", "", []),
        RpmUtil.getSpecFileFromRpm(rpm));

return success
