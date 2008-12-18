import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo

boolean success = true

File pkg = new File((File) basedir, "target/project-pkg-2-1.1-2.pkg")

success &= ShittyUtil.assertRelaxed(
        new PackageInfo( "project-pkg-2", "Hudson Slave", "application", "all", "1.1-2", null, null ),
        PkginfoUtil.getPackageInforForDevice(pkg));

success &= ShittyUtil.assertPkgEntries(pkg, [
        new PkgchkUtil.RegularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/atom-license.txt", "0644", "nobody", "nogroup", 49, 4473, null),
        new PkgchkUtil.RegularFile("/usr/share/hudson/license/dc-license.txt", "0644", "nobody", "nogroup", 1544, 59072, null),
        new PkgchkUtil.InstallationFile("pkginfo", 122, 0, null),
])
return success
