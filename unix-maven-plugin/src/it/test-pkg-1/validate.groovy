import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo

boolean success = true

File pkg = new File((File) basedir, "target/project-pkg-1-1.1-2.pkg")

success &= ShittyUtil.assertRelaxed( 
        new PackageInfo( "project-pkg-1", "Hudson", "application", "all", "1.1-2", null, null ),
        PkginfoUtil.getPackageInforForDevice(pkg));

// Ignore dates for now
success &= ShittyUtil.assertPkgEntries(pkg, [
        new PkgchkUtil.RegularFile("/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, null),
        new PkgchkUtil.InstallationFile("pkginfo", 116, 0, null),
])

return success
