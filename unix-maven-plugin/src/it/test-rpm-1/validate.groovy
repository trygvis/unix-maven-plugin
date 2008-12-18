import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.rpm.RpmUtil
import org.codehaus.mojo.unix.rpm.RpmUtil.SpecFile

boolean success = true

File rpm = new File((File) basedir, "target/project-rpm-1-1.1-2.rpm")
success &= ShittyUtil.assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/usr/share/hudson/lib/hudson.war", "hudson", "hudson", "----r--r--", 20623413, null),
])

success &= ShittyUtil.assertRelaxed(
        new SpecFile( "project-rpm-1", "1.1", 2, "Unnamed - bar:project-rpm-1:rpm:1.1-2", "BSD", "Application/Collectors", ""),
        RpmUtil.getSpecFileFromRpm(rpm));

return success
