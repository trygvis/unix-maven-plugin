package org.codehaus.mojo.unix.maven;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DpkgSpecificSettings
{
    private String priority;

    private String section;

    public String getPriority()
    {
        return priority;
    }

    public void setPriority( String priority )
    {
        this.priority = priority;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection( String section )
    {
        this.section = section;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
    }
}
