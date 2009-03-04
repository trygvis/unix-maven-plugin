package org.codehaus.mojo.unix.core;

import static fj.data.Option.some;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import static org.codehaus.mojo.unix.UnixFsObject.symlink;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;
import org.joda.time.LocalDateTime;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SymlinkOperation
    extends AssemblyOperation
{
    private final RelativePath source;

    private final String target;

    private final FileAttributes attributes;

    public SymlinkOperation( RelativePath source, String target, FileAttributes attributes )
    {
        validateNotNull( source, target, attributes );
        this.source = source;
        this.target = target;
        this.attributes = attributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        fileCollector.addSymlink( symlink( source, new LocalDateTime(), some( attributes ), target ) );
    }
}
