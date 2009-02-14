package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PkgSpecificSettings
{
    private String classes;

    private String[] extraPrototype = new String[0];

    public String getClasses()
    {
        return classes;
    }

    public String[] getExtraPrototype()
    {
        return extraPrototype;
    }
}
