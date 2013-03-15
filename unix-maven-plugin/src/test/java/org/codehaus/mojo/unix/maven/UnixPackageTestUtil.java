package org.codehaus.mojo.unix.maven;

import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import static java.util.regex.Pattern.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.apache.maven.plugin.logging.*;
import static org.codehaus.mojo.unix.FileAttributes.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.PackageParameters.*;
import static org.codehaus.mojo.unix.PackageVersion.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.io.LineEnding.*;
import org.codehaus.mojo.unix.io.fs.*;
import org.codehaus.mojo.unix.maven.plugin.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import static org.codehaus.mojo.unix.util.ScriptUtil.Strategy.*;
import static org.codehaus.plexus.PlexusTestCase.*;
import static org.codehaus.plexus.util.FileUtils.*;
import org.joda.time.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class UnixPackageTestUtil<UP extends UnixPackage<UP, PP>, PP extends UnixPackage.PreparedPackage>
{
    private final String id;

    private final PackagingFormat<UP> packagingFormat;

    private final LocalDateTime now = new LocalDateTime();

    private static final PackageVersion version = packageVersion( "1.0", "123", false, some( "1" ) );

    public final PackageParameters parameters =
        packageParameters( "mygroup", "myartifact", version, "id", "default-name", Option.<java.lang.String>none(),
                           EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "all" ).
            license( "BSD" );

    public UnixPackageTestUtil( String id, PackagingFormat<UP> packagingFormat )
    {
        this.id = id;
        this.packagingFormat = packagingFormat;
    }

    protected UP extraStuff( UP up )
    {
        return up;
    }

    public void testFiltering()
        throws Exception
    {
        LocalFs resources = new LocalFs( getTestFile( "src/test/resources/test-filtering" ) );
        LocalFs root = new LocalFs( getTestFile( "target/filtering-" + id ) );
        deleteDirectory( root.file );
        root.mkdir();
        LocalFs workingDirectory = root.resolve( "working-directory" );

        UP pkg = packagingFormat.start( new SystemStreamLog() ).
            parameters( parameters ).
            debug( true ).
            workingDirectory( workingDirectory );

        pkg = extraStuff( pkg );

        pkg.beforeAssembly( EMPTY, now );
        List<UnixFsObject.Replacer> replacers =
            single( new UnixFsObject.Replacer( quote( "${project.version}" ), "1.0" ) );
        UnixFsObject.RegularFile file =
            regularFile( relativePath( "/config.properties" ), now, 0, EMPTY, replacers, unix );
        pkg.addFile( resources.resolve( "config.properties" ), file );

        pkg.prepare( SINGLE );

        LocalFs config = workingDirectory.resolve( "assembly" ).resolve( "config.properties" );
        assertTrue( config.isFile() );
        assertEquals( 12, config.size() );
    }
}
