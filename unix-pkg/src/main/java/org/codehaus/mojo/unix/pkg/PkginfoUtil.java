package org.codehaus.mojo.unix.pkg;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;
import org.codehaus.mojo.unix.EqualsIgnoreNull;
import org.codehaus.mojo.unix.util.SystemCommand;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoUtil
{
    public static class PackageInfo
        implements EqualsIgnoreNull<PackageInfo>, LineProducer
    {
        public final String instance;

        public final String name;

        public final String category;

        public final String arch;

        public final String version;

        public final Option<String> desc;

        public final Option<String> pstamp;

        public PackageInfo( String instance, String name, String category, String arch, String version,
                            Option<String> desc, Option<String> pstamp )
        {
            validateNotNull( instance, name, category, arch, version, desc, pstamp );
            this.instance = instance;
            this.name = name;
            this.category = category;
            this.arch = arch;
            this.version = version;
            this.desc = desc;
            this.pstamp = pstamp;
        }

        public void streamTo( LineStreamWriter stream )
        {
            stream.
                add( " PKGINST: " + instance ).
                add( "    NAME: " + name ).
                add( "    ARCH: " + arch ).
                add( " VERSION: " + version ).
                add( "CATEGORY: " + category ).
                add( "    DESC: " + desc.orSome( "" ) ).
                add( "  PSTAMP: " + pstamp.orSome( "" ) );
        }

        public boolean equalsIgnoreNull( PackageInfo that )
        {
            return instance.equals( that.instance ) &&
                name.equals( that.name ) &&
                category.equals( that.category ) &&
                arch.equals( that.arch ) &&
                version.equals( that.version ) &&
                ( desc.isNone() || desc.some().equals( that.desc.some() ) ) &&
                ( pstamp.isNone() || pstamp.some().equals( that.pstamp.some() ) );
        }
    }

    public static Option<PackageInfo> getPackageInforForDevice( File device )
        throws IOException
    {
        return getPackageInforForDevice( device, null );
    }

    public static Option<PackageInfo> getPackageInforForDevice( File device, String instance )
        throws IOException
    {
        if ( !device.canRead() )
        {
            throw new FileNotFoundException( device.getAbsolutePath() );
        }

        PkginfoParser parser = new PkginfoParser();
        new SystemCommand().
            dumpCommandIf( true ).
            withStderrConsumer( parser ).
            withStdoutConsumer( parser ).
            setCommand( "pkginfo" ).
            addArgument( "-d" ).
            addArgument( device.getAbsolutePath() ).
            addArgument( "-l" ).
            addArgumentIfNotEmpty( instance ).
            execute().
            assertSuccess();

        // ( StringUtils.isNotEmpty( instance ) ? this : addArgument(instance) )

        return parser.getPackageInfo();
    }

    private static class PkginfoParser
        implements SystemCommand.LineConsumer
    {
        private Option<String> instance = none();

        private Option<String> name = none();

        private Option<String> category = none();

        private Option<String> arch = none();

        private Option<String> version = none();

        private Option<String> desc = none();

        private Option<String> pstamp = none();

        public void onLine( String line )
        {
            int i = line.indexOf( ':' );

            if ( i == -1 )
            {
                return;
            }

            String field = line.substring( 0, i ).trim();
            String value = line.substring( i + 1 ).trim();

            if ( "PKGINST".equals( field ) )
            {
                instance = some( value );
            }
            else if ( "NAME".equals( field ) )
            {
                name = some( value );
            }
            else if ( "CATEGORY".equals( field ) )
            {
                category = some( value );
            }
            else if ( "ARCH".equals( field ) )
            {
                arch = some( value );
            }
            else if ( "VERSION".equals( field ) )
            {
                version = some( value );
            }
            else if ( "DESC".equals( field ) )
            {
                desc = some( value );
            }
            else if ( "PSTAMP".equals( field ) )
            {
                pstamp = some( value );
            }
        }

        public Option<PackageInfo> getPackageInfo()
        {
            if ( instance.isNone() || name.isNone() || category.isNone() || arch.isNone() || version.isNone() )
            {
                return none();
            }

            return some( new PackageInfo( instance.some(), name.some(), category.some(), arch.some(), version.some(), desc, pstamp ) );
        }
    }
}
