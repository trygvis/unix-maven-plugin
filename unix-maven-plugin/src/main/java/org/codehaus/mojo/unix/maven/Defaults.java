package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Defaults
{
    private String fileUser = "nobody";

    private String fileGroup = "nogroup";

    private String fileMode = "0644";

    private String directoryUser = "nobody";

    private String directoryGroup = "nogroup";

    private String directoryMode = "0755";

    public String getFileUser()
    {
        return fileUser;
    }

    public void setFileUser( String fileUser )
    {
        this.fileUser = fileUser;
    }

    public String getFileGroup()
    {
        return fileGroup;
    }

    public void setFileGroup( String fileGroup )
    {
        this.fileGroup = fileGroup;
    }

    public String getFileMode()
    {
        return fileMode;
    }

    public void setFileMode( String fileMode )
    {
        this.fileMode = fileMode;
    }

    public String getDirectoryUser()
    {
        return directoryUser;
    }

    public void setDirectoryUser( String directoryUser )
    {
        this.directoryUser = directoryUser;
    }

    public String getDirectoryGroup()
    {
        return directoryGroup;
    }

    public void setDirectoryGroup( String directoryGroup )
    {
        this.directoryGroup = directoryGroup;
    }

    public String getDirectoryMode()
    {
        return directoryMode;
    }

    public void setDirectoryMode( String directoryMode )
    {
        this.directoryMode = directoryMode;
    }
}
