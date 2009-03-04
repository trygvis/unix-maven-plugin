import static org.codehaus.mojo.unix.maven.ShittyUtil.*
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.*
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo
import static fj.data.Option.*;

boolean success = true

File main = new File((File) basedir, "target/project-pkg-3-1.1-2.pkg")
pkginfo = PkginfoUtil.getPackageInforForDevice(main);

println "************************************************************************"
println "Hudson Master"

// No <description> inside <project>, no <description> inside <package> => null

success &= assertRelaxed(
        new PackageInfo( "project-pkg-3", "Hudson Master", "application", "all", "1.1-2", none(), none() ),
        PkginfoUtil.getPackageInforForDevice(main).some());

success &= assertPkgEntries(main, [
        regularFile("/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, none()),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, none()),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, none()),
//        new PkgchkUtil.RegularFile("/usr/share/hudson/server/README.txt", "0644", "root", "bin", 1544, 59072, null),
        installationFile("pkginfo", 150, 0, none()),
])

println "************************************************************************"
println "Hudson Slave"

// This has the <description> set on <package> too
File slave = new File((File) basedir, "target/project-pkg-3-slave-1.1-2.pkg")
success &= assertRelaxed(
        new PackageInfo( "project-pkg-3-slave", "Hudson Slave", "application", "all", "1.1-2", some("Hudson slave node"), none() ),
        PkginfoUtil.getPackageInforForDevice(slave).some());

success &= assertPkgEntries(slave, [
        regularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, none()),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, none()),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, none()),
        installationFile("pkginfo", 172, 0, none()),
])

return success
