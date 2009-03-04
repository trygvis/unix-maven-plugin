import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.*
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo
import static fj.data.Option.none;

boolean success = true

File pkg = new File((File) basedir, "target/project-pkg-2-1.1-2.pkg")

success &= ShittyUtil.assertRelaxed(
        new PackageInfo( "project-pkg-2", "Hudson Slave", "application", "all", "1.1-2", none(), none() ),
        PkginfoUtil.getPackageInforForDevice(pkg).some());

success &= ShittyUtil.assertPkgEntries(pkg, [
        regularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, none()),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "nobody", "nogroup", 49, 4473, none()),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "nobody", "nogroup", 1544, 59072, none()),
        installationFile("pkginfo", 149, 0, none()),
])
return success
