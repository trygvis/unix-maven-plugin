import org.codehaus.mojo.unix.maven.ShittyUtil
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.*
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo
import static fj.data.Option.none;

boolean success = true

File pkg = new File((File) basedir, "target/project-pkg-classes-1.1.pkg")

success &= ShittyUtil.assertRelaxed( 
        new PackageInfo( "project-pkg-classes", "Hudson", "application", "all", "1.1", none(), none()),
        PkginfoUtil.getPackageInforForDevice(pkg).some());

// Ignore dates for now
success &= ShittyUtil.assertPkgEntries(pkg, [
        directory("/opt", "17777777777", "?", "?", none()),
        directory("/opt/hudson", "0755", "nobody", "nogroup", none()),
        regularFile("/opt/hudson/hudson.war", "0644", "nobody", "nogroup", 20623413, 3301, none()),
        directory("/var", "17777777777", "?", "?", none()),
        directory("/var/lib", "0755", "nobody", "nogroup", none()),
        regularFile("/var/lib/app-method", "0644", "nobody", "nogroup", 30, 2290, none()),
        regularFile("/var/lib/app-manifest.xml", "0644", "nobody", "nogroup", 36, 3285, none()),
        installationFile("pkginfo", 151, 0, none()),
])

return success
