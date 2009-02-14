package org.codehaus.mojo.unix.rpm;

import org.codehaus.mojo.unix.util.SystemCommand;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Rpm
{
    private final String operation;

    private GeneralOptions generalOptions;

    private SelectOptions selectOptions;

    private QueryOptions queryOptions;

    private Rpm( String operation )
    {
        this.operation = operation;
    }

    public static GeneralOptions query()
    {
        Rpm rpm = new Rpm( "query" );
        rpm.generalOptions = rpm.new GeneralOptions();
        rpm.selectOptions = rpm.new SelectOptions();
        rpm.queryOptions = rpm.new QueryOptions();
        return rpm.generalOptions;
    }

    public class GeneralOptions
    {
        File basedir;

        boolean debug;

        boolean verbose;

        public GeneralOptions setBasedir( File basedir )
        {
            this.basedir = basedir;
            return this;
        }

        public GeneralOptions setDebug( boolean debug )
        {
            this.debug = debug;
            return this;
        }

        public GeneralOptions setVerbose( boolean verbose )
        {
            this.verbose = verbose;
            return this;
        }

        public SelectOptions withSelectOptions()
        {
            return selectOptions;
        }
    }

    public class SelectOptions
    {
        boolean all;

        File packageFile;

        public QueryOptions withQuery()
        {
            return queryOptions;
        }

        public SelectOptions setAll( boolean all )
        {
            this.all = all;
            return this;
        }

        public SelectOptions setPackage( File packageFile )
        {
            this.packageFile = packageFile;
            return this;
        }
    }

    public class QueryOptions
    {
        public boolean list;

        public QueryOptions setList( boolean list )
        {
            this.list = list;
            return this;
        }

        public void execute()
            throws IOException
        {
            Rpm.this.execute();
        }
    }

    private void setGeneralOptions( SystemCommand systemCommand, GeneralOptions generalOptions )
    {
        if ( generalOptions.verbose )
        {
            systemCommand.addArgument( "-v" );
        }
    }

    private void setSelectOptions( SystemCommand systemCommand, SelectOptions selectOptions )
    {
        if ( selectOptions.all )
        {
            systemCommand.addArgument( "--all" );
        }

        if ( selectOptions.packageFile != null )
        {
            systemCommand.addArgument( "--package" );
            systemCommand.addArgument( selectOptions.packageFile.getAbsolutePath() );
        }
    }

    private void setQueryOptions( SystemCommand systemCommand, QueryOptions queryOptions )
    {
        if ( queryOptions.list )
        {
            systemCommand.addArgument( "--list" );
        }
    }

    public void execute()
        throws IOException
    {
        SystemCommand command = new SystemCommand().
            withNoStderrConsumerUnless( generalOptions.debug ).
            withNoStdoutConsumerUnless( generalOptions.debug ).
            setBasedir( generalOptions.basedir ).
            setCommand( "rpm" ).
            addArgument( "--" + operation );

        setGeneralOptions( command, generalOptions );
        setSelectOptions( command, selectOptions );
        setQueryOptions( command, queryOptions );

        command.execute().
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "rpm" );
    }
}
