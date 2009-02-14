package org.codehaus.mojo.unix.maven;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Package
{
    private String id = "default";

    private String name;

    private String packageName;

    private String description;

    private AssemblyOperation[] assembly = new AssemblyOperation[0];

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public AssemblyOperation[] getAssembly()
    {
        return assembly;
    }

    public void setAssembly( AssemblyOperation[] assembly )
    {
        this.assembly = assembly != null ? assembly : new AssemblyOperation[0];
    }
}
