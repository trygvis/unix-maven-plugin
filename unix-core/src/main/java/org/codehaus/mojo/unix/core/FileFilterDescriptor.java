package org.codehaus.mojo.unix.core;

import fj.data.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import org.codehaus.mojo.unix.io.*;
import static org.codehaus.mojo.unix.io.IncludeExcludeFilter.*;

public class FileFilterDescriptor
{
    public final IncludeExcludeFilter selector;

    public final List<Filter> filters;

    public FileFilterDescriptor( List<String> includes, List<String> excludes, List<Filter> filters )
    {
        selector = includeExcludeFilter().
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        this.filters = filters;
    }
}
