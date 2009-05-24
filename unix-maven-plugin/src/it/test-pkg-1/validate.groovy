import org.codehaus.mojo.unix.maven.ShittyUtil
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.*
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
        directory("/opt", "17777777777", "?", "?", none()),
        directory("/opt/hudson", "0755", "nobody", "nogroup", none()),
        regularFile("/opt/hudson/hudson.war", "0666", "hudson", "hudson", 20623413, 3301, none()),
        directory("/usr", "17777777777", "?", "?", none()),
        directory("/usr/share", "0755", "nobody", "nogroup", none()),
        directory("/usr/share/hudson", "0755", "nobody", "nogroup", none()),
        regularFile("/usr/share/hudson/README.txt", "0644", "nobody", "nogroup", 38, 3568, none()),
        directory("/var", "17777777777", "?", "?", none()),
        directory("/var/log", "0755", "nobody", "nogroup", none()),
        symlink("/var/log/hudson", "/var/opt/hudson/log"),
        installationFile("checkinstall", 28, 2563, none()),
        installationFile("compver", 0, 0, none()),
        installationFile("copyright", 24, 2150, none()),
        installationFile("depend", 0, 0, none()),
        installationFile("pkginfo", 147, 0, none()),
        installationFile("request", 46, 4055, none()),
        installationFile("space", 0, 0, none()),
])

return success
