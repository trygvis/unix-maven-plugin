package org.codehaus.mojo.unix.maven.plugin;

import org.codehaus.mojo.unix.*;

public class Regex
{
    public String pattern;
    public String replacement;

    public UnixFsObject.Replacer toReplacer()
    {
        return new UnixFsObject.Replacer( pattern, replacement);
    }
}
