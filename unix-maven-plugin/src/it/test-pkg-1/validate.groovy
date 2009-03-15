import org.codehaus.mojo.unix.maven.ShittyUtil
import org.codehaus.mojo.unix.pkg.PkgchkUtil
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.*;
import org.codehaus.mojo.unix.pkg.PkginfoUtil
import org.codehaus.mojo.unix.pkg.PkginfoUtil.PackageInfo
import static fj.data.Option.none;

boolean success = true

File pkg = new File((File) basedir, "target/project-pkg-1-1.1-2.pkg")

success &= ShittyUtil.assertRelaxed( 
        new PackageInfo( "project-pkg-1", "Hudson", "application", "all", "1.1-2", none(), none()),
        PkginfoUtil.getPackageInforForDevice(pkg).some());

// Ignore dates for now
success &= ShittyUtil.assertPkgEntries(pkg, [
        directory("/opt/hudson", "0755", "nobody", "nogroup", none()),
        regularFile("/opt/hudson/hudson.war", "0666", "hudson", "hudson", 20623413, 3301, none()),
        symlink("/var/log/hudson", "/var/opt/hudson/log"),
        installationFile("checkinstall", 28, 2563, none()),
        installationFile("compver", 0, 0, none()),
        installationFile("copyright", 24, 2150, none()),
        installationFile("depend", 0, 0, none()),
        installationFile("pkginfo", 143, 0, none()),
        installationFile("request", 46, 4055, none()),
        installationFile("space", 0, 0, none()),
])

return success
