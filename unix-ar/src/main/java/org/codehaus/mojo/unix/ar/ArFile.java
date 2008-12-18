package org.codehaus.mojo.unix.ar;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArFile
{
    protected File file;
    protected String name;
    protected long lastModified;
    protected int ownerId;
    protected int groupId;
    protected int mode;
    protected long size;

    public String getName()
    {
        return name;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public int getOwnerId()
    {
        return ownerId;
    }

    public int getGroupId()
    {
        return groupId;
    }

    public int getMode()
    {
        return mode;
    }

    public long getSize()
    {
        return size;
    }

    public static ArFile fromFile( File file )
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }
        ArFile arFile = new ArFile();
        arFile.file = file;
        if ( arFile.name == null )
        {
            arFile.name = file.getName();
        }
        arFile.mode = 420; // 664
        arFile.lastModified = file.lastModified() / 1000;
        arFile.size = file.length();

        if ( arFile.name.length() > 16 )
        {
            throw new FileNameTooLongException();
        }

        return arFile;
    }
}
