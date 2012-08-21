package org.codehaus.mojo.unix.core;

import fj.data.*;
import org.codehaus.mojo.unix.io.*;
import static org.codehaus.mojo.unix.io.IncludeExcludeFilter.*;
import org.codehaus.mojo.unix.util.*;

public class FileFilterDescriptor
{
    private final IncludeExcludeFilter filter;

    public FileFilterDescriptor( List<String> includes, List<String> excludes )
    {
        filter = includeExcludeFilter().
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();
    }

    public boolean matches( RelativePath targetName )
    {
        return filter.matches( targetName );
    }
}
