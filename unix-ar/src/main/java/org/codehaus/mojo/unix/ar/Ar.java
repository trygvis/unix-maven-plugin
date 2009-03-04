package org.codehaus.mojo.unix.ar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Ar
{
    public static NewAr create()
    {
        return new NewAr();
    }

    public static CloseableIterable read( File file )
        throws IOException
    {
        return new ArReader( file );
    }

    public static class NewAr
    {
        private final List<ArFile> files = new ArrayList<ArFile>();

        public class NewArFile
        {
            ArFile file;

            public NewArFile withUid( int uid )
            {
                file.ownerId = uid;

                return this;
            }

            public NewArFile withGid( int gid )
            {
                file.groupId = gid;

                return this;
            }

            public NewAr done()
            {
                files.add( file );
                return NewAr.this;
            }
        }

        public NewAr addFileDone( File file )
        {
            files.add( ArFile.fromFile( file ) );

            return this;
        }

        public NewArFile addFile( File file )
        {
            NewArFile f = new NewArFile();
            f.file = ArFile.fromFile( file );
            return f;
        }

        public void storeToFile( File file )
            throws IOException
        {
            ArWriter writer = null;
            try
            {
                writer = new ArWriter( file );

                for ( ArFile arFile : files )
                {
                    writer.add(arFile);
                }
            }
            finally
            {
                ArUtil.close( writer );
            }
        }
    }
}
