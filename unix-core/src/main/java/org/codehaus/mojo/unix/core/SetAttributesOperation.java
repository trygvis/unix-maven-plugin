package org.codehaus.mojo.unix.core;

import fj.F2;
import static fj.Function.curry;
import fj.data.Option;
import static fj.data.Option.none;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.Validate.validateNotNull;
import org.codehaus.mojo.unix.util.vfs.IncludeExcludeFileSelector;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributesOperation
    extends AssemblyOperation
{
    private final RelativePath to;

    private final Option<FileAttributes> fileAttributes;

    private final Option<FileAttributes> directoryAttributes;

    private final IncludeExcludeFileSelector selector;

    public SetAttributesOperation( RelativePath to, List<String> includes, List<String> excludes,
                                   Option<FileAttributes> fileAttributes, Option<FileAttributes> directoryAttributes )
    {
        validateNotNull( to, includes, excludes, fileAttributes, directoryAttributes );
        this.to = to;
        this.fileAttributes = fileAttributes;
        this.directoryAttributes = directoryAttributes;

        selector = IncludeExcludeFileSelector.build( null ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        if ( fileAttributes.isSome() )
        {
            fileCollector.applyOnFiles( curry( setAttributes, fileAttributes ) );
        }

        if ( directoryAttributes.isSome() )
        {
            fileCollector.applyOnDirectories( curry( setAttributes, directoryAttributes ) );
        }
    }

    F2<Option<FileAttributes>, RelativePath, Option<FileAttributes>> setAttributes =
        new F2<Option<FileAttributes>, RelativePath, Option<FileAttributes>>()
        {
            public Option<FileAttributes> f( Option<FileAttributes> fileAttributes, RelativePath path )
            {
                if ( !path.startsWith( to ) || !selector.matches( path.string ) )
                {
                    return none();
                }

                return fileAttributes;
            }
        };
}
