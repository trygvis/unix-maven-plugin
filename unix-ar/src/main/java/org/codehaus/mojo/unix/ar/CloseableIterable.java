package org.codehaus.mojo.unix.ar;

import java.util.Iterator;

public interface CloseableIterable
{
    Iterator iterator();

    void close();
}
