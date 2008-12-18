package org.codehaus.mojo.unix.maven;

/**
 * A utility class to contain all configuration settings for a packaging mojo.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackagingMojoParameters
{
    public final String name;
    public final String version;
    public final Integer revision;
    public final String description;
    public final String contact;
    public final String contactEmail;
    public final String architecture;
    public AssemblyOperation[] assembly;
    public Package[] packages;

    public PackagingMojoParameters( String name,
                                    String version,
                                    Integer revision,
                                    String description,
                                    String contact,
                                    String contactEmail,
                                    String architecture,
                                    AssemblyOperation[] assembly,
                                    Package[] packages )
    {
        this.name = name;
        this.version = version;
        this.revision = revision;
        this.description = description;
        this.contact = contact;
        this.contactEmail = contactEmail;
        this.architecture = architecture;
        this.assembly = assembly;
        this.packages = packages;
    }
}
