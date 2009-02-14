package org.codehaus.mojo.unix.ar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
        private List files = new ArrayList();

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

                for ( Iterator it = files.iterator(); it.hasNext(); )
                {
                    ArFile arFile = (ArFile) it.next();
                    writer.add( arFile );
                }
            }
            finally
            {
                ArUtil.close( writer );
            }
        }
    }

    /*
    public static InputStream openFile(File arFile, String fileName) throws IOException {
        ArReader reader = null;
        try {
            reader = new ArReader(arFile);
            for (ArFile file : reader) {
                if (file.getName().equals(fileName)) {
                    return loadFile(file);
                }
            }

            throw new NoSuchFileInArchiveException();
        } finally {
            close(reader);
        }
    }
    */
}
