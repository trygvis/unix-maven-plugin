package org.codehaus.mojo.unix.maven;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RpmSpecificSettings
{
    private String softwareGroup;

    public String getSoftwareGroup()
    {
        return softwareGroup;
    }

    public void setSoftwareGroup( String softwareGroup )
    {
        this.softwareGroup = softwareGroup;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
    }
}
