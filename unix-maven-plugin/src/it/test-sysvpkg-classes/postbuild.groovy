import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.sysvpkg.PkgchkUtil.*
import org.codehaus.mojo.unix.sysvpkg.PkginfoUtil
import static fj.data.Option.*
import fj.data.Option
import org.joda.time.LocalDateTime
import org.codehaus.mojo.unix.sysvpkg.PkginfoFile;

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File pkg = new File((File) basedir, "target/project-sysvpkg-classes-1.1.pkg")

success &= assertRelaxed(
        new PkginfoFile("all", "application", "Hudson", "project-sysvpkg-classes", "1.1"),
        PkginfoUtil.getPackageInfoForDevice(pkg).some(), packageInfoEqual);

// Ignore dates for now
success &= assertSysvPkgEntries(pkg, [
        directory("/opt", "17777777777", "?", "?", ldtNone),
        directory("/opt/hudson", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/opt/hudson/hudson.war", "0644", "nobody", "nogroup", 20623413, 3301, ldtNone),
        directory("/var", "17777777777", "?", "?", ldtNone),
        directory("/var/lib", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/var/lib/app-method", "0644", "nobody", "nogroup", 30, 2290, ldtNone),
        regularFile("/var/lib/app-manifest.xml", "0644", "nobody", "nogroup", 36, 3285, ldtNone),
        installationFile("pkginfo", 155, 0, ldtNone),
])

return success
