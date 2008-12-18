package org.codehaus.mojo.unix.util;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Executes a system command.
 * <p/>
 * Typical usage:
 * <pre>
 * new SystemCommand().
 *     setBaseDir(directory).
 *     setCommand("dpkg").
 *     addArgument("-b").
 *     addArgument("deb").
 *     addArgument(debFileName).
 *     execute();
 * </pre>
 *
 * TODO: Support executing commands, asserting exit code and consuming stdout into a String.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SystemCommand.java 1483 2006-02-13 09:19:48Z trygvis $
 */
public class SystemCommand
{
    public static interface LineConsumer
    {
        void onLine( String line )
            throws IOException;
    }

    public static class ToStringLineConsumer
        implements LineConsumer
    {
        private StringBuffer buffer;

        public ToStringLineConsumer()
        {
            this( 1024 );
        }

        public ToStringLineConsumer( int size )
        {
            buffer = new StringBuffer( size );
        }

        public void onLine( String line )
            throws IOException
        {
            buffer.append( line );
        }

        public String toString()
        {
            return buffer.toString();
        }
    }

    public static class ExecutionResult
    {
        public final int exitCode;
        private final String command;

        ExecutionResult( int exitCode, String command )
        {
            this.exitCode = exitCode;
            this.command = command;
        }

        public ExecutionResult assertSuccess()
            throws IOException
        {
            if ( exitCode != 0 )
            {
                throw new IOException( "Command '" + command + "' returned a non-null exit code: " + exitCode );
            }

            return this;
        }

        public ExecutionResult assertSuccess( String exceptionMessage )
            throws IOException
        {
            if ( exitCode != 0 )
            {
                throw new IOException( exceptionMessage );
            }

            return this;
        }
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    private File basedir;

    private String command;

    private List arguments;

    private List environment;

    private boolean debug;

    private InputStreamHandler stderrHandler;

    private InputStreamHandler stdoutHandler;

    public SystemCommand()
    {
        arguments = new ArrayList();
    }

    public SystemCommand setCommand( String command )
    {
        this.command = command;
        return this;
    }

    public SystemCommand setBasedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    public SystemCommand addArgument( String argument )
    {
        arguments.add( argument );
        return this;
    }

    public SystemCommand addArgumentIf( boolean b, String argument )
    {
        if ( !b )
        {
            return this;
        }

        arguments.add( argument );
        return this;
    }

    public SystemCommand addArgumentIfNotEmpty( String argument )
    {
        if ( argument == null || argument.trim().length() == 0 )
        {
            return this;
        }

        arguments.add( argument );
        return this;
    }

    public SystemCommand addEnviroment( String variable )
    {
        if ( environment == null )
        {
            environment = new ArrayList();
        }

        environment.add( variable );
        return this;
    }

    public SystemCommand dumpCommandIf( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public SystemCommand dumpOutputIf( boolean dump )
    {
        if ( !dump )
        {
            return this;
        }

        return withStderrConsumer( System.err ).
            withStdoutConsumer( System.out );
    }

    public SystemCommand withStderrConsumer( OutputStream consumer )
    {
        if ( stderrHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stderrHandler = new OutputStreamInputStreamHandler( consumer );
        return this;
    }

    public SystemCommand withStderrConsumer( LineConsumer lineConsumer )
    {
        if ( stderrHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stderrHandler = new LineConsumerInputStreamHandler( lineConsumer );
        return this;
    }

    public SystemCommand withStdoutConsumer( OutputStream consumer )
    {
        if ( stdoutHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stdoutHandler = new OutputStreamInputStreamHandler( consumer );
        return this;
    }

    public SystemCommand withStdoutConsumer( LineConsumer consumer )
    {
        if ( stdoutHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stdoutHandler = new LineConsumerInputStreamHandler( consumer );
        return this;
    }

    public ExecutionResult execute()
        throws IOException
    {
        if ( basedir == null )
        {
            basedir = new File( "/" ).getAbsoluteFile();
        }

        if ( command == null )
        {
            throw new IOException( "Missing field 'command'" );
        }

        if ( debug )
        {
            System.err.println( "Executing '" + command + "' with arguments (one argument per line):" );
            for ( Iterator it = arguments.iterator(); it.hasNext(); )
            {
                System.err.println( it.next().toString() );
            }
            System.err.println( "Executing command in directory: " + basedir );
        }

        if ( !basedir.isDirectory() )
        {
            throw new IOException( "The basedir must be a directory: '" + basedir + "'." );
        }

        arguments.add( 0, command );

        return new Execution( command, arguments, environment, basedir, stderrHandler, stdoutHandler ).run();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static boolean available(String command)
    {
        try
        {
            SystemCommand.ToStringLineConsumer stdout = new SystemCommand.ToStringLineConsumer();

            new SystemCommand().setCommand( "which" ).
                addArgument( command ).
                withStdoutConsumer( stdout ).
                execute().
                assertSuccess();

            return new File( stdout.toString() ).canRead();
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static class Execution
    {
        private final String command;
        private final List arguments;
        private final List environment;
        private final File basedir;
        private InputStreamHandler stderrHandler;
        private InputStreamHandler stdoutHandler;

        public Pumper stderrPumper;
        public Pumper stdoutPumper;
        public Process process;

        private Execution( String command, List arguments, List environment, File basedir,
                           InputStreamHandler stderrHandler, InputStreamHandler stdoutHandler )
        {
            this.command = command;
            this.arguments = arguments;
            this.environment = environment;
            this.basedir = basedir;
            this.stderrHandler = stderrHandler;
            this.stdoutHandler = stdoutHandler;
        }

        public ExecutionResult run()
            throws IOException
        {
            String[] args = (String[]) arguments.toArray( new String[arguments.size()] );
            String[] env = null;

            if ( environment != null )
            {
                env = (String[]) environment.toArray( new String[environment.size()] );
            }

            process = Runtime.getRuntime().exec( args, env, basedir );

            process.getOutputStream().close();

            setUpStdErrConsumer();

            setUpStdOutConsumer();

            try
            {
                process.waitFor();
            }
            catch ( InterruptedException e )
            {
                IOException ex = new IOException( "Interrupted while waiting for process" );
                ex.initCause( e );
                throw ex;
            }

            if ( stderrPumper != null )
            {
                try
                {
                    stderrPumper.join();
                }
                catch ( InterruptedException e )
                {
                    // ignore
                }
            }

            if ( stdoutPumper != null )
            {
                try
                {
                    stdoutPumper.join();
                }
                catch ( InterruptedException e )
                {
                    // ignore
                }
            }

            return new ExecutionResult( process.exitValue(), command );
        }

        private void setUpStdErrConsumer()
            throws IOException
        {
            if ( stderrHandler == null )
            {
                // For some reason this seems to result in random failures of some tools like pkgmk
                // process.getErrorStream().close();
                return;
            }

            stderrPumper = new Pumper( command + ": stderr-pumper", process.getErrorStream(), stderrHandler );
            stderrPumper.start();
        }

        private void setUpStdOutConsumer()
            throws IOException
        {
            if ( stdoutHandler == null )
            {
                // For some reason this seems to result in random failures of some tools like pkgmk
                // process.getInputStream().close();
                return;
            }

            stdoutPumper = new Pumper( command + ": stdout-pumper", process.getInputStream(), stdoutHandler );
            stdoutPumper.start();
        }
    }

    private static interface InputStreamHandler
    {
        void handle( InputStream inputStream )
            throws IOException;
    }

    private static class OutputStreamInputStreamHandler
        implements InputStreamHandler
    {
        private OutputStream outputStream;

        private OutputStreamInputStreamHandler( OutputStream outputStream )
        {
            this.outputStream = outputStream;
        }

        public void handle( InputStream inputStream )
            throws IOException
        {
            IOUtil.copy( inputStream, outputStream );
            outputStream.flush();
        }
    }

    private static class LineConsumerInputStreamHandler
        implements InputStreamHandler
    {
        private LineConsumer lineConsumer;

        public LineConsumerInputStreamHandler( LineConsumer lineConsumer )
        {
            this.lineConsumer = lineConsumer;
        }

        public void handle( InputStream inputStream )
            throws IOException
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            String line = reader.readLine();

            while ( line != null )
            {
                lineConsumer.onLine( line );

                line = reader.readLine();
            }
        }
    }

    private static class Pumper
        extends Thread
    {
        private InputStream inputStream;

        private InputStreamHandler handler;

        public Pumper( String threadName, InputStream inputStream, InputStreamHandler handler )
        {
            super( threadName );
            this.inputStream = inputStream;
            this.handler = handler;
        }

        public void run()
        {
            try
            {
                handler.handle( inputStream );
            }
            catch ( IOException e )
            {
                // ignore and die
            }
            finally
            {
                IOUtil.close( inputStream );
            }
        }
    }
}
