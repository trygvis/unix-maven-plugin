package org.codehaus.mojo.unix.maven;

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

import fj.data.List;
import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public final class ScriptUtil
{
    private String format;

    private ScriptFile preInstall;
    private ScriptFile postInstall;
    private ScriptFile preRemove;
    private ScriptFile postRemove;
    private List<ScriptFile> customScripts = List.nil();

    private final static class ScriptFile
    {
        private String commonName;
        private String specificName;

        private ScriptFile( String commonName, String specificName )
        {
            this.commonName = commonName;
            this.specificName = specificName;
        }

        public File copyScript( File common, File specific, File toDir )
            throws IOException
        {
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            File toFile = new File( toDir, specificName );

            File commonScript = null;
            if ( commonName != null )
            {
                commonScript = new File( common, commonName );
            }
            File specificScript = new File( specific, specificName );

            FileInputStream fis = null;
            FileOutputStream fos = null;
            try
            {
                if ( commonScript != null && commonScript.canRead() )
                {
                    fis = new FileInputStream( commonScript );
                    IOUtil.copy( fis, content );
                    fis.close();
                }

                if ( specificScript.canRead() )
                {
                    fis = new FileInputStream( specificScript );
                    IOUtil.copy( fis, content );
                    fis.close();
                }

                // No size == 0 optimalization here. If the script exist in the source directory, we want it in the
                // output too.

                if ( ( commonScript == null || !commonScript.canRead() ) && !specificScript.canRead() )
                {
                    return null;
                }

                if ( !toFile.getParentFile().isDirectory() )
                {
                    if ( !toFile.getParentFile().mkdirs() )
                    {
                        throw new IOException( "Unable to create direcetory '" + toFile.getParentFile() + "'." );
                    }
                }

                // TODO: Check that the files differ to prevent writing new files.
                // TODO: Set the timestamp of the file to the newest of the two files read.

                fos = new FileOutputStream( toFile );
                IOUtil.copy( new ByteArrayInputStream( content.toByteArray() ), fos );
            }
            finally
            {
                IOUtil.close( fis );
                IOUtil.close( fos );
            }

            return toFile;
        }
    }

    public final static class ScriptUtilBuilder
    {
        private ScriptUtil scriptUtil = new ScriptUtil();

        public ScriptUtilBuilder format( String format )
        {
            scriptUtil.format = format;

            return this;
        }

        public ScriptUtil build()
        {
            return scriptUtil;
        }

        public ScriptUtilBuilder setPreInstall( String name )
        {
            scriptUtil.preInstall = new ScriptFile( "pre-install", name );
            return this;
        }

        public ScriptUtilBuilder setPostInstall( String name )
        {
            scriptUtil.postInstall = new ScriptFile( "post-install", name );
            return this;
        }

        public ScriptUtilBuilder setPreRemove( String name )
        {
            scriptUtil.preRemove = new ScriptFile( "pre-remove", name );
            return this;
        }

        public ScriptUtilBuilder setPostRemove( String name )
        {
            scriptUtil.postRemove = new ScriptFile( "post-remove", name );
            return this;
        }

        public ScriptUtilBuilder addCustomScript( String specificName )
        {
            scriptUtil.customScripts = scriptUtil.customScripts.cons( new ScriptFile( null, specificName ) );
            return this;
        }
    }

    public final static class Execution
    {
        File preInstall;
        File postInstall;
        File preRemove;
        File postRemove;
        private List<File> customScripts = List.nil();

        public File getPreInstall()
        {
            return preInstall;
        }

        public boolean hasPreInstall()
        {
            return preInstall != null && preInstall.canRead();
        }

        public File getPostInstall()
        {
            return postInstall;
        }

        public boolean hasPostInstall()
        {
            return postInstall != null && postInstall.canRead();
        }

        public File getPreRemove()
        {
            return preRemove;
        }

        public boolean hasPreRemove()
        {
            return preRemove != null && preRemove.canRead();
        }

        public File getPostRemove()
        {
            return postRemove;
        }

        public boolean hasPostRemove()
        {
            return postRemove != null && postRemove.canRead();
        }

        public List<File> getCustomScripts()
        {
            return customScripts;
        }
    }

    public Execution copyScripts( File basedir, final File toDir )
        throws IOException
    {
        final File common = new File( basedir, "src/main/unix/scripts/common" );
        final File specific = new File( basedir, "src/main/unix/scripts/" + format );

        Execution execution = new Execution();

        execution.preInstall = preInstall.copyScript( common, specific, toDir );
        execution.postInstall = postInstall.copyScript( common, specific, toDir );
        execution.preRemove = preRemove.copyScript( common, specific, toDir );
        execution.postRemove = postRemove.copyScript( common, specific, toDir );

        for (ScriptFile customScript : customScripts )
        {
            execution.customScripts = execution.customScripts.cons( customScript.copyScript( common, specific, toDir ) );
        }
        execution.customScripts = execution.customScripts.reverse();

        return execution;
    }
}
