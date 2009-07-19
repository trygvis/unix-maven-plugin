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
import org.apache.maven.artifact.*;
import org.apache.maven.model.*;
import org.apache.maven.project.*;
import static org.codehaus.mojo.unix.util.Validate.*;

import java.io.*;
import java.util.List;
import java.util.*;
import java.util.Set;

/**
 * A small wrapper around a MavenProject instance to make testing easier.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: MojoHelper.java 9221 2009-03-15 22:52:14Z trygvis $
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

    public final Set<Artifact> artifacts;

    public final List<License> licenses;

    public final Map<String, Artifact> artifactConflictIdMap;

    public MavenProjectWrapper( String groupId, String artifactId, String version, Artifact artifact, String name,
                                String description, File basedir, File buildDirectory, Set<Artifact> artifacts,
                                List<License> licenses, Map<String, Artifact> artifactConflictIdMap )
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
        this.artifacts = artifacts;
        this.licenses = licenses;
        this.artifactConflictIdMap = artifactConflictIdMap;
    }

    public static MavenProjectWrapper mavenProjectWrapper( MavenProject project )
    {
        Map<String, Artifact> artifactConflictIdMap = new java.util.HashMap<String, Artifact>();

        //noinspection unchecked
        for ( Artifact artifact : (java.util.Set<Artifact>) project.getArtifacts() )
        {
            artifactConflictIdMap.put( artifact.getDependencyConflictId(), artifact );
        }

        //noinspection unchecked
        return new MavenProjectWrapper( project.getGroupId(), project.getArtifactId(), project.getVersion(),
                                        project.getArtifact(), project.getName(), project.getDescription(),
                                        project.getBasedir(), new File( project.getBuild().getDirectory() ),
                                        project.getArtifacts(), project.getLicenses(), artifactConflictIdMap );
    }
}
