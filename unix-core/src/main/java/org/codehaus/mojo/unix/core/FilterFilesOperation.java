package org.codehaus.mojo.unix.core;

import fj.*;
import static fj.Show.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.UnixFsObject.Filter.*;
import org.codehaus.mojo.unix.util.line.*;

import java.io.*;

public class FilterFilesOperation
    implements AssemblyOperation
{
    private final List<FileFilterDescriptor> filters;

    public FilterFilesOperation( List<FileFilterDescriptor> filters )
    {
        this.filters = filters;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        fileCollector.apply( new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject object )
            {
                if ( !( object instanceof RegularFile ) )
                {
                    return none();
                }

                RegularFile f = (RegularFile) object;

                List<Filter> matched = nil();

                for ( FileFilterDescriptor descriptor : filters )
                {
                    if ( descriptor.selector.matches( f.path ) )
                    {
                        matched = matched.append( descriptor.filters );
                    }
                }

                return matched.isEmpty() ?
                    Option.<UnixFsObject>none() :
                    some( object.withFilters( matched ) );
            }
        } );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        throw new RuntimeException( "Not implemented" );
    }
}
