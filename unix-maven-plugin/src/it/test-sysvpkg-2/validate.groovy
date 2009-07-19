import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.sysvpkg.PkgchkUtil.*
import org.codehaus.mojo.unix.sysvpkg.PkginfoFile
import org.codehaus.mojo.unix.sysvpkg.PkginfoUtil
import static fj.data.Option.*
import fj.data.Option
import org.joda.time.LocalDateTime

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File pkg = new File((File) basedir, "target/project-sysvpkg-2-1.1-2.pkg")

success &= assertRelaxed(
        new PkginfoFile( "all", "application", "Hudson Slave", "project-sysvpkg-2", "1.1-2"),
        PkginfoUtil.getPackageInfoForDevice(pkg).some(), packageInfoEqual);

success &= assertSysvPkgEntries(pkg, [
        directory("/usr", "17777777777", "?", "?", ldtNone),
        directory("/usr/share", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson/lib", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, ldtNone),
        directory("/usr/share/hudson/license", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "nobody", "nogroup", 49, 4473, ldtNone),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "nobody", "nogroup", 1544, 59072, ldtNone),
        installationFile("pkginfo", 153, 0, ldtNone),
])
return success
