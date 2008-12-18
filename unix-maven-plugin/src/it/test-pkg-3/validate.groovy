import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo

boolean success = true

File main = new File((File) basedir, "target/project-pkg-3-1.1-2.pkg")
pkginfo = PkginfoUtil.getPackageInforForDevice(main);

// No <description> inside <project>, no <description> inside <package> => null

success &= ShittyUtil.assertRelaxed(
        new PackageInfo( "project-pkg-3", "Hudson Master", "application", "all", "1.1-2", null, null ),
        PkginfoUtil.getPackageInforForDevice(main));

success &= ShittyUtil.assertPkgEntries(main, [
        new PkgchkUtil.RegularFile("/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, null),
//        new PkgchkUtil.RegularFile("/usr/share/hudson/server/README.txt", "0644", "root", "bin", 1544, 59072, null),
        new PkgchkUtil.InstallationFile("pkginfo", 123, 0, null),
])

// This has the <description> set on <package> too

File slave = new File((File) basedir, "target/project-pkg-3-slave-1.1-2.pkg")
success &= ShittyUtil.assertRelaxed(
        new PackageInfo( "project-pkg-3-slave", "Hudson Slave", "application", "all", "1.1-2", "Hudson slave node", null ),
        PkginfoUtil.getPackageInforForDevice(slave));

success &= ShittyUtil.assertPkgEntries(slave, [
        new PkgchkUtil.RegularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, null),
//        new PkgchkUtil.RegularFile("/usr/share/hudson/slave/README.txt", "0644", "root", "bin", 1544, 59072, null),
        new PkgchkUtil.InstallationFile("pkginfo", 145, 0, null),
])

return success
