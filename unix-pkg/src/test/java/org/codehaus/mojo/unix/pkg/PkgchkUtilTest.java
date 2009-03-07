package org.codehaus.mojo.unix.pkg;

import fj.F2;
import fj.Function;
import fj.Unit;
import fj.data.List;
import fj.data.Option;
import junit.framework.TestCase;
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.FileInfo;
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.directory;
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.installationFile;
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.regularFile;
import static org.codehaus.mojo.unix.pkg.PkgchkUtil.symlink;
import org.codehaus.mojo.unix.util.UnixUtil;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.joda.time.LocalDateTime;

public class PkgchkUtilTest
    extends TestCase
{
    public void testBasic()
    {
        LineFile file = new LineFile();
        file.
            add( "## Checking control scripts." ).
            add( "## Checking package objects." ).
            add( "Pathname: /usr/share/hudson/lib/hudson.war" ).
            add( "Type: regular file" ).
            add( "Expected mode: 0644" ).
            add( "Expected owner: hudson" ).
            add( "Expected group: hudson" ).
            add( "Expected file size (bytes): 20623413" ).
            add( "Expected sum(1) of contents: 3301" ).
            add( "Expected last modification: Jan 30 15:11:40 2009" ).
            add( "Current status: installed" ).
            add( "" ).
            add( "Pathname: /var/log/hudson" ).
            add( "Type: symbolic link" ).
            add( "Source of link: /var/opt/hudson/log" ).
            add( "Current status: installed" ).
            add( "" ).
            add( "Pathname: /usr/lib" ).
            add( "Type: directory" ).
            add( "Expected mode: 0755" ).
            add( "Expected owner: root" ).
            add( "Expected group: bin" ).
            add( "" ).
            add( "Pathname: pkginfo" ).
            add( "Type: installation file" ).
            add( "Expected file size (bytes): 143" ).
            add( "Expected sum(1) of contents: 11066" ).
            add( "Expected last modification: Feb 25 15:13:52 2009" ).
            add( "" ).
            add( "## Checking is complete." );

        PkgchkUtil.PkgchkParser parser = new PkgchkUtil.PkgchkParser();

        for ( String line : file )
        {
            parser.onLine( line );
        }

        Option<LocalDateTime> date = PkgchkUtil.parser.f( "Jan 30 15:11:40 2009" ).map( UnixUtil.toLocalDateTime );
        Option<LocalDateTime> date2 = PkgchkUtil.parser.f( "Feb 25 15:13:52 2009" ).map( UnixUtil.toLocalDateTime );
        assertTrue( date.isSome() );
        assertTrue( date2.isSome() );

        List<PkgchkUtil.FileInfo> expected = List.<PkgchkUtil.FileInfo>nil().
            cons( regularFile( "/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, date ) ).
            cons( symlink( "/var/log/hudson", "/var/opt/hudson/log" ) ).
            cons( directory( "/usr/lib", "0755", "root", "bin", Option.<LocalDateTime>none() ) ).
            cons( installationFile( "pkginfo", 143, 11066, date2 ) ).
            reverse();

        List<FileInfo> actual = parser.getList();

        assertEquals( expected.length(), actual.length() );

        expected.zipWith( actual, Function.curry( new F2<FileInfo, FileInfo, Object>()
        {
            public Object f( FileInfo expected, FileInfo actual )
            {
                if ( !expected.equalsIgnoreNull( actual ) )
                {
                    LineFile lines = new LineFile();

                    lines.add( "expected:" );
                    expected.streamTo( lines );

                    lines.add();
                    lines.add( "actual:" );
                    actual.streamTo( lines );

                    fail( lines.toString() );
                }
                return Unit.unit();
            }
        } ) );
    }
}
