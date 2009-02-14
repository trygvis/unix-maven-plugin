package org.codehaus.mojo.unix.pkg;

import junit.framework.TestCase;
import org.codehaus.mojo.unix.util.line.LineFile;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFileTest extends TestCase {
    public void testParsing() {
        LineFile pkginfoStrings = (LineFile)(new LineFile().
            add("        PKGINST:  project-pkg-1").
            add("           NAME:  Hudson").
            add("       CATEGORY:  application").
            add("           ARCH:  all").
            add("        VERSION:  1.1-2").
            add("         PSTAMP:  20090129.134909").
            add("          EMAIL:  trygvis@codehaus.org").
            add("         STATUS:  spooled").
            add("          FILES:        3 spooled pathnames").
            add("                        2 package information files").
            add("                    40281 blocks used (approx)"));

        PkginfoFile expected = new PkginfoFile();
        expected.packageName = "project-pkg-1";
        expected.name = "Hudson";
        expected.category = "application";
        expected.arch = "all";
        expected.version = "1.1-2";
        expected.pstamp = "20090129.134909";
        expected.email = "trygvis@codehaus.org";
        assertEquals(expected.toString(), PkginfoFile.factory.fromStream( pkginfoStrings ).toString() );
    }
}
