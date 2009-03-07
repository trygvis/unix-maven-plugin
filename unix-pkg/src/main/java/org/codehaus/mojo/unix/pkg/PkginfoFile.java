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

import org.codehaus.mojo.unix.util.line.LineConsumer;
import org.codehaus.mojo.unix.util.line.LineFile;
import org.codehaus.mojo.unix.util.line.LineProducer;
import org.codehaus.mojo.unix.util.line.LineStreamWriter;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The logic creating the pkginfo file.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkginfoFile
    implements LineProducer
{
    public String packageName;
    public String classifier;
    public String version;
    public String pstamp;
    public String name;
    public String desc;
    public String email;
    public String arch;
    public String classes = "none";
    public String category = "application";

    public void streamTo( LineStreamWriter streamWriter)
    {
        streamWriter.
            add( "PKG=" + packageName ).
            add( "NAME=" + StringUtils.clean( name ) ).
            add( "DESC=" + StringUtils.clean( desc ) ).
            addIf( StringUtils.isNotEmpty( email ), "EMAIL=" + email ).
            add( "VERSION=" + version ).
            add( "PSTAMP=" + pstamp ).
            addIf( StringUtils.isNotEmpty( classes ), "CLASSES=" + (StringUtils.isNotEmpty( classes ) ? classes : "none") ).
            addIf( StringUtils.isNotEmpty( arch ), "ARCH=" + (StringUtils.isNotEmpty( arch ) ? arch : "all") ).
            addIf( StringUtils.isEmpty( arch ), "ARCH=all" ).
            add( "CATEGORY=" + StringUtils.clean( category ) );
    }

    public static final LineConsumer<PkginfoFile> factory = new LineConsumer<PkginfoFile>()
    {
        public PkginfoFile fromStream( Iterable<String> lines )
        {
            PkginfoFile pkginfoFile = new PkginfoFile();

            for (String line : lines) {
                int i = line.indexOf( ':' );

                if ( i == -1 )
                {
                    continue;
                }

                String field = line.substring( 0, i ).trim();
                String value = line.substring( i + 1 ).trim();

                if ( "PKGINST".equals( field ) )
                {
                    pkginfoFile.packageName = value;
                }
                else if ( "NAME".equals( field ) )
                {
                    pkginfoFile.name = value;
                }
                else if ( "CATEGORY".equals( field ) )
                {
                    pkginfoFile.category = value;
                }
                else if ( "ARCH".equals( field ) )
                {
                    pkginfoFile.arch = value;
                }
                else if ( "VERSION".equals( field ) )
                {
                    pkginfoFile.version = value;
                }
                else if ( "DESC".equals( field ) )
                {
                    pkginfoFile.desc = value;
                }
                else if ( "EMAIL".equals( field ) )
                {
                    pkginfoFile.email = value;
                }
                else if ( "PSTAMP".equals( field ) )
                {
                    pkginfoFile.pstamp = value;
                }
            }

            return pkginfoFile;
        }
    };

    public String toString()
    {
        LineFile file = new LineFile();
        streamTo( file );
        return file.toString();
    }

    public String getPkgName( File pkginfoFile )
        throws IOException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream( pkginfoFile );

            Properties properties = new Properties();
            properties.load( inputStream );

            String packageName = properties.getProperty( "PKG" );

            if ( packageName == null )
            {
                throw new IOException( "Could not read package name (PKG) from pkginfo file: '" +
                    pkginfoFile.getAbsolutePath() + "'." );
            }

            return packageName;
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }
}
