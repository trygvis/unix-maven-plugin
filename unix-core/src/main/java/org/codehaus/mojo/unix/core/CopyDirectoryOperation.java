package org.codehaus.mojo.unix.core;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.codehaus.mojo.unix.FileAttributes;
import org.codehaus.mojo.unix.FileCollector;
import org.codehaus.mojo.unix.util.RelativePath;
import static org.codehaus.mojo.unix.util.RelativePath.fromString;
import org.codehaus.mojo.unix.util.vfs.IncludeExcludeFileSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CopyDirectoryOperation
    extends AssemblyOperation
{
    private final FileObject from;

    private final RelativePath to;

    private final List<String> includes;

    private final List<String> excludes;

    private final String patternString;

    private final String replacement;

    private final FileAttributes fileAttributes;

    private final FileAttributes directoryAttributes;

    public CopyDirectoryOperation( FileObject from, RelativePath to, List<String> includes, List<String> excludes,
                                   String patternString, String replacement, FileAttributes fileAttributes,
                                   FileAttributes directoryAttributes )
    {
        this.from = from;
        this.to = to;
        this.includes = includes;
        this.excludes = excludes;
        this.patternString = patternString;
        this.replacement = replacement;
        this.fileAttributes = fileAttributes;
        this.directoryAttributes = directoryAttributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        Pattern pattern = patternString != null ? Pattern.compile( patternString ) : null;

        IncludeExcludeFileSelector selector = IncludeExcludeFileSelector.build( from.getName() ).
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        List<FileObject> files = new ArrayList<FileObject>();
        from.findFiles( selector, true, files );

        for ( FileObject f : files )
        {
            if ( f.getName().getBaseName().equals( "" ) )
            {
                continue;
            }

            String relativeName = from.getName().getRelativeName( f.getName() );

            // Transform the path if the pattern is set. The input path will always have a leading slash
            // to make it possible to write more natural expressions.
            // With this one can write "/server-1.0.0/(.*)" => $1
            if ( pattern != null )
            {
                relativeName = pattern.matcher( fromString( relativeName ).asAbsolutePath() ).replaceAll( replacement );
            }

            if ( f.getType() == FileType.FILE )
            {
                fileCollector.addFile( f, fromFileObject( to.add( relativeName ), f, fileAttributes ) );
            }
            else if ( f.getType() == FileType.FOLDER )
            {
                fileCollector.addDirectory( dirFromFileObject( to.add( relativeName ), f, directoryAttributes ) );
            }
        }
    }
}
