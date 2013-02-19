package org.codehaus.mojo.unix.maven.plugin;

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

import fj.data.*;
import static fj.data.Option.*;
import static java.util.Collections.*;
import org.apache.maven.artifact.*;
import org.apache.maven.execution.*;
import org.apache.maven.model.*;
import org.apache.maven.project.*;
import static org.codehaus.mojo.unix.util.Validate.*;
import org.joda.time.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import java.util.Map.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * A small wrapper around a MavenProject instance to make testing easier.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class MavenProjectWrapper
{
    public final String groupId;

    public final String artifactId;

    public final String version;

    public final Artifact artifact;

    public final String name;

    public final Option<String> description;

    public final File basedir;

    public final File buildDirectory;

    public final LocalDateTime timestamp;

    public final Set<Artifact> artifacts;

    public final List<License> licenses;

    public final ArtifactMap artifactMap;

    public final SortedMap<String, String> properties;

    public MavenProjectWrapper( String groupId, String artifactId, String version, Artifact artifact, String name,
                                String description, File basedir, File buildDirectory, LocalDateTime timestamp,
                                Set<Artifact> artifacts, List<License> licenses, ArtifactMap artifactMap,
                                SortedMap<String, String> properties )
    {
        validateNotNull( groupId, artifactId, version, name );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.artifact = artifact;
        this.name = name;
        this.description = fromNull( description );
        this.basedir = basedir;
        this.buildDirectory = buildDirectory;
        this.timestamp = timestamp;
        this.artifacts = artifacts;
        this.licenses = licenses;
        this.artifactMap = artifactMap;
        this.properties = properties;
    }

    public static MavenProjectWrapper mavenProjectWrapper( final MavenProject project, MavenSession session )
    {
        SortedMap<String, String> properties = new TreeMap<String, String>();

        // This is perhaps not ideal. Maven uses reflection to dynamically extract properties from the project
        // when interpolating each file. This uses a static list that doesn't contain the project.* properties, except
        // the new we hard-code support for.
        //
        // The user can work around this like this:
        // <properties>
        //   <project.build.directory>${project.build.directory}</project.build.directory>
        // </properties>
        //
        // If this has to change, the properties has to be a F<String, String> and interpolation tokens ("${" and "}")
        // has to be defined. Doable but too painful for now.
        properties.putAll( toMap( session.getSystemProperties() ) );
        properties.putAll( toMap( session.getUserProperties() ) );
        properties.putAll( toMap( project.getProperties() ) );
        properties.put( "project.groupId", project.getGroupId() );
        properties.put( "project.artifactId", project.getArtifactId() );
        properties.put( "project.version", project.getVersion() );

        return new MavenProjectWrapper( project.getGroupId(), project.getArtifactId(), project.getVersion(),
                                        project.getArtifact(), project.getName(), project.getDescription(),
                                        project.getBasedir(), new File( project.getBuild().getDirectory() ),
                                        new LocalDateTime(), project.getArtifacts(), project.getLicenses(),
                                        new ArtifactMap( project.getArtifacts() ),
                                        unmodifiableSortedMap( properties ) );
    }

    private static Map<String, String> toMap( Properties properties )
    {
        Map<String, String> map = new HashMap<String, String>();
        for ( Entry<Object, Object> entry : properties.entrySet() )
        {
            map.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        return map;
    }

    public static class ArtifactMap
    {
        private final Map<String, Artifact> artifacts = new HashMap<String, Artifact>();

        public ArtifactMap( Set<Artifact> artifacts )
        {
            for ( Artifact artifact : artifacts )
            {
                this.artifacts.put( artifact.getDependencyConflictId(), artifact );
            }
        }

        public File validateArtifact( String artifact )
            throws UnknownArtifactException
        {
            Artifact a = artifacts.get( artifact );

            if ( a != null )
            {
                return a.getFile();
            }

            a = artifacts.get( artifact + ":jar" );

            if ( a != null )
            {
                return a.getFile();
            }

            throw new UnknownArtifactException( artifact, artifacts );
        }
    }
}
