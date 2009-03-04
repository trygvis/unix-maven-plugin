package org.codehaus.mojo.unix.pkg.prototype;

import fj.data.Option;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.UnixUtil.noneBoolean;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: AbstractPrototypeEntry.java 7323 2008-07-26 14:58:37Z trygvis $
 */
public class SymlinkEntry
    extends PrototypeEntry
{
    private String to;

    protected SymlinkEntry( Option<String> pkgClass, RelativePath path, String to )
    {
        super( pkgClass, noneBoolean, path );
        validateNotNull( to );

        this.to = to;
    }

    public String generatePrototypeLine()
    {
        return "s " + pkgClass + " " + getPath() + "=" + to;
    }
}
