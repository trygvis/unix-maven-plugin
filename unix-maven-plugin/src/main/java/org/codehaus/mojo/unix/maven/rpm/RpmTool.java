package org.codehaus.mojo.unix.maven.rpm;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class RpmTool
{
    public String groupId;
    public String artifactId;

    public String getBaseName()
    {
        return groupId;
    }
}
