package org.codehaus.mojo.unix.util;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

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
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
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
        public final int exitValue;
        public final String command;

        ExecutionResult( int exitValue, String command )
        {
            this.exitValue = exitValue;
            this.command = command;
        }

        public ExecutionResult assertSuccess()
            throws IOException
        {
            if ( exitValue != 0 )
            {
                throw new IOException( "Command '" + command + "' returned a non-null exit code: " + exitValue );
            }

            return this;
        }

        public ExecutionResult assertSuccess( String exceptionMessage )
            throws IOException
        {
            if ( exitValue != 0 )
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

    private List<String> arguments;

    private List<String> environment;

    private boolean debug;

    private CommandOutputHandler stderrHandler;

    private CommandOutputHandler stdoutHandler;

    public SystemCommand()
    {
        arguments = new ArrayList<String>();
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

    public SystemCommand addArguments( List<String> strings )
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
            environment = new ArrayList<String>();
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
            for ( String argument : arguments ) {
                System.err.println( argument );
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
//                dumpCommandIf( true ).
                addArgument( command ).
                withStdoutConsumer( stdout ).
                execute().
                assertSuccess();

//            System.out.println( "stdout = " + stdout );
//            System.out.println( "new File( stdout.toString() ).canRead() = " + new File( stdout.toString() ).canRead() );
//            System.out.println( "new File( stdout.toString() ).getCanonicalFile().canRead() = " + new File( stdout.toString() ).getCanonicalFile().canRead() );

            return new File( stdout.toString() ).getCanonicalFile().canRead();
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
        private final List<String> arguments;
        private final List<String> environment;
        private final File basedir;
        private final boolean debug;
        private final CommandOutputHandler stderrHandler;
        private final CommandOutputHandler stdoutHandler;

        public Process process;

        private Execution( String command, List<String> arguments, List<String> environment, File basedir,
                           boolean debug, CommandOutputHandler stderrHandler, CommandOutputHandler stdoutHandler )
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
            String[] args = arguments.toArray( new String[arguments.size()] );
            String[] env = null;

            if ( environment != null )
            {
                env = environment.toArray( new String[environment.size()] );
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

            int exitValue = process.exitValue();

            if ( debug )
            {
                System.out.println( "Command completed: " + command + ", exit value: " + exitValue );
            }

            return new ExecutionResult( exitValue, command );
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
