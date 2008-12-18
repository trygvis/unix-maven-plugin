package org.codehaus.mojo.unix.maven.dpkg;

import org.apache.maven.artifact.Artifact;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class DebianDependency
{
    private String groupId;

    private String artifactId;

    private String version;

    private File file;

    public DebianDependency()
    {
    }

    public DebianDependency( Artifact artifact )
    {
        this.groupId = artifact.getGroupId();

        this.artifactId = artifact.getArtifactId();

        this.version = "=" + artifact.getVersion();

        this.file = artifact.getFile();
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile( File file )
    {
        this.file = file;
    }
}
