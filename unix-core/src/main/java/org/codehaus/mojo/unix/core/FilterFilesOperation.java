package org.codehaus.mojo.unix.core;

import fj.*;
import fj.data.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.core.AssemblyOperationUtil.*;
import org.codehaus.mojo.unix.io.*;
import static org.codehaus.mojo.unix.io.IncludeExcludeFilter.*;
import org.codehaus.mojo.unix.util.line.*;

import java.io.*;

/**
 * TODO: support basedir parameter like SetAttributesOperation.
 * TODO: Is it really correct that filtered files should retain the old timestamp?
 */
public class FilterFilesOperation
    implements AssemblyOperation
{
    public final List<String> includes;
    public final List<String> excludes;
    public final List<Replacer> replacers;
    public final LineEnding lineEnding;

    public FilterFilesOperation( List<String> includes, List<String> excludes, List<Replacer> replacers, LineEnding lineEnding )
    {
        this.includes = includes;
        this.excludes = excludes;
        this.replacers = replacers;
        this.lineEnding = lineEnding;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        final IncludeExcludeFilter selector = includeExcludeFilter().
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        fileCollector.apply( new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject object )
            {
                if ( !( object instanceof RegularFile ) )
                {
                    return none();
                }

                RegularFile f = (RegularFile) object;

                if ( !selector.matches( f.path ) )
                {
                    return none();
                }

                return some( object.addReplacers( replacers, lineEnding ) );
            }
        } );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Filter Files:" );
        streamIncludesAndExcludes( streamWriter, includes, excludes );
        streamWriter.add( " Replacers:" );
        for ( Replacer replacer : replacers )
        {
            streamWriter.add( "  " + replacer.toString() );
        }
    }
}
