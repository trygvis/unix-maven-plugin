package org.codehaus.mojo.unix.core;

import fj.*;
import fj.data.*;
import org.codehaus.mojo.unix.*;
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
//        fileCollector.setFilter( new F2<UnixFsObject, List<FileFilterDescriptor>, List<FileFilterDescriptor>>()
//        {
//            public List<FileFilterDescriptor> f( UnixFsObject unixFsObject, List<FileFilterDescriptor> fileFilterDescriptors )
//            {
//
//            }
//        } );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        throw new RuntimeException( "Not implemented" );
    }
}
