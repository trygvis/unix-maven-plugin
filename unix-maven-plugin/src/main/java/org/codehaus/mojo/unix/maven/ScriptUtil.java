package org.codehaus.mojo.unix.maven;

import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private List customScripts = new ArrayList();

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
            scriptUtil.customScripts.add( new ScriptFile( null, specificName ) );
            return this;
        }
    }

    public final static class Execution
    {
        File preInstall;
        File postInstall;
        File preRemove;
        File postRemove;

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
    }

    public Execution copyScripts( File basedir, File toDir )
        throws IOException
    {
        File common = new File( basedir, "src/main/unix/scripts/common" );
        File specific = new File( basedir, "src/main/unix/scripts/" + format );

        Execution execution = new Execution();

        execution.preInstall = preInstall.copyScript( common, specific, toDir );
        execution.postInstall = postInstall.copyScript( common, specific, toDir );
        execution.preRemove = preRemove.copyScript( common, specific, toDir );
        execution.postRemove = postRemove.copyScript( common, specific, toDir );

        for ( Iterator it = customScripts.iterator(); it.hasNext(); )
        {
            ( (ScriptFile) it.next() ).copyScript( common, specific, toDir );
        }

        return execution;
    }
}
