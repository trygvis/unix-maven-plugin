package org.codehaus.mojo.unix;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import fj.data.*;
import static fj.data.Option.*;
import static org.codehaus.mojo.unix.util.Validate.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: MojoHelper.java 9656 2009-04-29 20:36:37Z trygvis $
 */
public class PackageParameters
{
    public final String groupId;

    public final String artifactId;

    public final PackageVersion version;

    /**
     * A quasi-unique id. Typically artifact id (+ classifier).
     */
    public final String id;

    /**
     * A single-line description of the package.
     */
    public final String name;

    public final Option<String> classifier;

    /**
     * A multi-line description of the package.
     */
    public final Option<String> description;

    public final Option<String> contact;

    public final Option<String> contactEmail;

    public final Option<String> license;

    public final Option<String> architecture;

    public final FileAttributes defaultFileAttributes;

    public final FileAttributes defaultDirectoryAttributes;

    public PackageParameters( String groupId, String artifactId, PackageVersion version, String id, String name,
                              FileAttributes defaultFileAttributes, FileAttributes defaultDirectoryAttributes,
                              Option<String> classifier, Option<String> description, Option<String> contact,
                              Option<String> contactEmail, Option<String> license, Option<String> architecture )
    {
        validateNotNull( groupId, artifactId, version, id, name, defaultFileAttributes, defaultDirectoryAttributes,
            classifier, description, contact, contactEmail, license, architecture );

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.id = id;
        this.name = name;
        this.classifier = classifier;
        this.description = description;
        this.contact = contact;
        this.contactEmail = contactEmail;
        this.license = license;
        this.architecture = architecture;
        this.defaultFileAttributes = defaultFileAttributes;
        this.defaultDirectoryAttributes = defaultDirectoryAttributes;
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static PackageParameters packageParameters( String groupId, String artifactId, PackageVersion version,
                                                       String id, String name, Option<String> classifier,
                                                       FileAttributes defaultFileAttributes,
                                                       FileAttributes defaultDirectoryAttributes)
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, Option.<String>none(), Option.<String>none(),
                                      Option.<String>none(), Option.<String>none(), Option.<String>none() );
    }

    public PackageParameters name( String name )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }

    public PackageParameters description( String description )
    {
        return description( fromNull( description ) );
    }

    public PackageParameters description( Option<String> description )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }

    public PackageParameters contact( String contact )
    {
        return contact( fromNull( contact ) );
    }

    public PackageParameters contact( Option<String> contact )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }

    public PackageParameters contactEmail( String contactEmail )
    {
        return contactEmail( fromNull( contactEmail ) );
    }

    public PackageParameters contactEmail( Option<String> contactEmail )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }

    public PackageParameters license( String license )
    {
        return license( fromNull( license ) );
    }

    public PackageParameters license( Option<String> license )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }

    public PackageParameters architecture( String architecture )
    {
        return architecture( fromNull( architecture ) );
    }

    public PackageParameters architecture( Option<String> architecture )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact, contactEmail,
                                      license, architecture );
    }
}
