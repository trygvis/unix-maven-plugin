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

import org.codehaus.mojo.unix.util.line.AbstractLineStreamWriter;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Executes a system command in a similar fasion to backtick (`) in sh and ruby.
 * <p>
 * Standard output and error will be send to System.out and System.err by default.
 * </p>
 * <p>
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
 * <p/>
 *
 * TODO: Create FileLineConsumer that puts all the output to a file.
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

    public static class StringBufferLineConsumer
        implements LineConsumer
    {
        public final StringBuffer buffer;

        public StringBufferLineConsumer()
        {
            this( 1024 );
        }

        public StringBufferLineConsumer( int size )
        {
            buffer = new StringBuffer( size );
        }

        public void onLine( String line )
            throws IOException
        {
            buffer.
                append( line ).
                append( AbstractLineStreamWriter.EOL );
        }

        public String toString()
        {
            return buffer.toString();
        }
    }

    public static class StringListLineConsumer
        implements LineConsumer
    {
        public final List<String> strings;

        public StringListLineConsumer()
        {
            this.strings = new LinkedList<String>();
        }

        public StringListLineConsumer( List<String> strings )
        {
            this.strings = strings;
        }

        public void onLine( String line )
            throws IOException
        {
            strings.add( line );
        }

        public List<String> getStrings()
        {
            return strings;
        }
    }

    public static class ExecutionResult
    {
        public final int exitCode;
        public final String command;

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

    private static final CommandOutputHandler nullOutputHandler = new NullCommandOutputHandler();

    private static final CommandOutputHandler DEFAULT_STDERR_OUTPUT_HANDLER = new OutputStreamCommandOutputHandler( System.err );

    private static final CommandOutputHandler DEFAULT_STDOUT_OUTPUT_HANDLER = new OutputStreamCommandOutputHandler(System.out );

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    private File basedir;

    private String command;

    private List arguments;

    private List environment;

    private boolean debug;

    private CommandOutputHandler stderrHandler;

    private CommandOutputHandler stdoutHandler;

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

    public SystemCommand addArguments(List strings)
    {
        arguments.addAll(strings);
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

    public SystemCommand addArgumentIfNotEmpty(String stringToCheck, String argument)
    {
        return addArgumentIf( StringUtils.isEmpty( stringToCheck ), argument );
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

    // -----------------------------------------------------------------------
    // Stderr
    // -----------------------------------------------------------------------

    public SystemCommand withNoStderrConsumer()
    {
        return setStderrCommandOutputHandler( nullOutputHandler );
    }

    public SystemCommand withNoStderrConsumerIf( boolean flag )
    {
        return flag ? setStderrCommandOutputHandler( nullOutputHandler ) : this;
    }

    public SystemCommand withNoStderrConsumerUnless( boolean flag )
    {
        return withNoStderrConsumerIf( !flag );
    }

    public SystemCommand withStderrConsumer( OutputStream consumer )
    {
        return setStderrCommandOutputHandler( new OutputStreamCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStderrConsumerUnless( OutputStream consumer, boolean flag )
    {
        return !flag ? withStderrConsumer( consumer ) : setStderrCommandOutputHandler( nullOutputHandler );
    }

    public SystemCommand withStderrConsumer( LineConsumer consumer )
    {
        return setStderrCommandOutputHandler( new LineConsumerCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStderrConsumerUnless( LineConsumer consumer, boolean flag )
    {
        return !flag ? withStderrConsumer( consumer ) : setStderrCommandOutputHandler( nullOutputHandler );
    }

    // -----------------------------------------------------------------------
    // Stdout
    // -----------------------------------------------------------------------

    public SystemCommand withNoStdoutConsumer()
    {
        return setStdoutCommandOutputHandler( nullOutputHandler );
    }

    public SystemCommand withNoStdoutConsumerIf( boolean flag )
    {
        return flag ? setStdoutCommandOutputHandler( nullOutputHandler ) : this;
    }

    public SystemCommand withNoStdoutConsumerUnless( boolean flag )
    {
        return withNoStdoutConsumerIf( !flag );
    }

    public SystemCommand withStdoutConsumer( OutputStream consumer )
    {
        return setStdoutCommandOutputHandler( new OutputStreamCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStdoutConsumerUnless( OutputStream consumer, boolean flag )
    {
        return !flag ? withStdoutConsumer( consumer ) : setStdoutCommandOutputHandler( nullOutputHandler );
    }

    public SystemCommand withStdoutConsumer( LineConsumer consumer )
    {
        return setStdoutCommandOutputHandler( new LineConsumerCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStdoutConsumerUnless( LineConsumer consumer, boolean flag )
    {
        return !flag ? withStdoutConsumer( consumer ) : setStdoutCommandOutputHandler( nullOutputHandler );
    }

    private SystemCommand setStderrCommandOutputHandler( CommandOutputHandler outputHandler )
    {
        if ( stderrHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stderrHandler = outputHandler;
        return this;
    }

    private SystemCommand setStdoutCommandOutputHandler( CommandOutputHandler outputHandler )
    {
        if ( stdoutHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stdoutHandler = outputHandler;
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
            throw new IOException( "Basedir must be a directory: '" + basedir + "'." );
        }

        arguments.add( 0, command );

        return new Execution( command, arguments, environment, basedir, debug,
                stderrHandler != null ? stderrHandler : DEFAULT_STDERR_OUTPUT_HANDLER,
                stdoutHandler != null ? stdoutHandler : DEFAULT_STDOUT_OUTPUT_HANDLER).run();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    /**
     * Utility method to check if a command is available.
     *
     * Executes "which" and asserts that the file exist. It does not check if the file is executable.
     */
    public static boolean available( String command )
    {
        try
        {
            StringBufferLineConsumer stdout = new StringBufferLineConsumer();

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
        private final boolean debug;
        private final CommandOutputHandler stderrHandler;
        private final CommandOutputHandler stdoutHandler;

        public Process process;

        private Execution( String command, List arguments, List environment, File basedir, boolean debug,
                           CommandOutputHandler stderrHandler, CommandOutputHandler stdoutHandler )
        {
            this.command = command;
            this.arguments = arguments;
            this.environment = environment;
            this.basedir = basedir;
            this.debug = debug;
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

            process.getOutputStream().close(); // Close stdin

            stderrHandler.setup( command + ": stderr", process.getErrorStream() );

            stdoutHandler.setup( command + ": stdout", process.getInputStream() );

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

            stderrHandler.join();
            stdoutHandler.join();

            if ( debug )
            {
                System.out.println( "Command completed: " + command );
            }

            return new ExecutionResult( process.exitValue(), command );
        }
    }

    private static abstract interface CommandOutputHandler
    {
        void setup(String threadName, InputStream inputStream );
        void join();
    }

    private static abstract class ThreadCommandOutputHandler
        implements CommandOutputHandler
    {
        private Thread thread;

        abstract void handle( InputStream inputStream )
            throws IOException;

        public void setup(String threadName, final InputStream inputStream )
        {
            thread = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        handle( inputStream );
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
            }, threadName);
            thread.start();
        }

        public void join()
        {
            try
            {
                if(thread == null)
                {
                    return;
                }

                thread.join();
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }

    private static class NullCommandOutputHandler
        implements CommandOutputHandler
    {
        public void setup(String threadName, InputStream inputStream)
        {
            IOUtil.close( inputStream );
        }

        public void join()
        {
        }
    }

    private static class OutputStreamCommandOutputHandler
        extends ThreadCommandOutputHandler
    {
        private OutputStream outputStream;

        private OutputStreamCommandOutputHandler( OutputStream outputStream )
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

    private static class LineConsumerCommandOutputHandler
        extends ThreadCommandOutputHandler
    {
        private LineConsumer lineConsumer;

        public LineConsumerCommandOutputHandler( LineConsumer lineConsumer )
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
}
