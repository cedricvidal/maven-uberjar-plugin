package biz.vidal.maven.plugins.uberjar;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.IOUtil;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import biz.vidal.shrinkwrap.uberjar.api.UberjarArchive;

/**
 * Build an Uberjar containing the current project with dependencies embedded.
 * 
 * @author <a href="cedric at vidal dot biz">Cedric Vidal</a>
 * @goal uberjar
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class UberjarMojo extends AbstractMojo {
    /**
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Directory containing the classes and resource files that should be
     * packaged into the JAR.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * @component
     * @required
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * Remote repositories which will be searched for source attachments.
     * 
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    protected List remoteArtifactRepositories;

    /**
     * Local maven repository.
     * 
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * Artifact factory, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * Artifact resolver, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;

    /**
     * The destination directory for the uberjar artifact.
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * @parameter
     */
    private ArtifactModel classworldsBootArtifact;

    /**
     * @parameter
     */
    private ArtifactModel classworldsArtifact;

    /**
     * Classifier to add to the uberjar generated. If given, the artifact will
     * be an attachment instead.
     * 
     * @parameter expression="${classifier}"
     */
    private String classifier;

    /**
     * The name of the classifier used in case the uberjar artifact is attached.
     * 
     * @parameter expression="${mainClass}"
     */
    private String mainClass;

    /**
     * The path to the output file for the uberjar artifact. When this parameter
     * is set, the created archive will neither replace the project's main
     * artifact nor will it be attached. Hence, this parameter causes the
     * parameters {@link #finalName} and {@link #classifier} to be ignored when
     * used.
     * 
     * @parameter
     */
    private File outputFile;

    /**
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {

        if (classworldsBootArtifact == null) {
            classworldsBootArtifact = new ArtifactModel();
            classworldsBootArtifact.setGroupId("classworlds");
            classworldsBootArtifact.setArtifactId("classworlds-boot");
            classworldsBootArtifact.setVersion("1.0");
        } else {
            checkArtifact(classworldsBootArtifact, "classworldsBootArtifact");
        }

        if (classworldsArtifact == null) {
            classworldsArtifact = new ArtifactModel();
            classworldsArtifact.setGroupId("classworlds");
            classworldsArtifact.setArtifactId("classworlds");
            classworldsArtifact.setVersion("1.1");
        } else {
            checkArtifact(classworldsArtifact, "classworldsArtifact");
        }

        final Set<Artifact> artifacts = project.getArtifacts();
        Set<File> artifactFiles = new LinkedHashSet<File>();
        for (Artifact artifact : artifacts) {
            artifactFiles.add(artifact.getFile());
        }

        Artifact classworldsArt = createArtifact(classworldsArtifact);
        Artifact bootArt = createArtifact(classworldsBootArtifact);

        resolve(classworldsArt);
        resolve(bootArt);

        JavaArchive classworldsJar = createFromZipFile(JavaArchive.class, classworldsArt.getFile());
        JavaArchive bootJar = createFromZipFile(JavaArchive.class, bootArt.getFile());
        String appJarName = project.getArtifactId() + "." + project.getArtifact().getArtifactHandler().getExtension();
        JavaArchive appJar = create(JavaArchive.class, appJarName).addAsResource(classesDirectory, "/");

        UberjarArchive uberJar = create(UberjarArchive.class).merge(bootJar).addAsLibrary(appJar).addAsLibraries(artifactFiles.toArray(new File[] {}))
                .setMainClass(mainClass).setClassworlds(classworldsJar).createConfiguration();

        File outputJar = (outputFile != null) ? outputFile : uberjarArtifactFileWithClassifier();
        uberJar.as(ZipExporter.class).exportTo(outputJar, true);

        // Now add our extra resources
        try {
            if (outputFile == null) {
                boolean renamed = false;

                if (classifier != null) {
                    getLog().info("Attaching uberjar artifact.");
                    projectHelper.attachArtifact(project, project.getArtifact().getType(), classifier, outputJar);
                } else if (!renamed) {
                    getLog().info("Replacing original artifact with uberjar artifact.");
                    File originalArtifact = project.getArtifact().getFile();
                    replaceFile(originalArtifact, outputJar);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating uberjar jar: " + e.getMessage(), e);
        }
    }

    private void checkArtifact(ArtifactModel artifact, String name) throws MojoExecutionException {
        if (artifact.getGroupId() == null || artifact.getArtifactId() == null || artifact.getVersion() == null) {
            throw new MojoExecutionException(name + " requires artifactId, groupId and version");
        }
    }

    private Artifact createArtifact(ArtifactModel artifact) {
        return artifactFactory.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "compile", "jar");
    }

    private void resolve(Artifact artifact) throws MojoExecutionException {
        try {
            artifactResolver.resolve(artifact, remoteArtifactRepositories, localRepository);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Could not resolve " + artifact, e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Could not find " + artifact, e);
        }
    }

    private void replaceFile(File oldFile, File newFile) throws MojoExecutionException {
        getLog().info("Replacing " + oldFile + " with " + newFile);

        if (!newFile.renameTo(oldFile)) {
            // try a gc to see if an unclosed stream needs garbage collecting
            System.gc();
            System.gc();

            if (!newFile.renameTo(oldFile)) {
                // Still didn't work. We'll do a copy
                try {
                    FileOutputStream fout = new FileOutputStream(oldFile);
                    FileInputStream fin = new FileInputStream(newFile);
                    try {
                        IOUtil.copy(fin, fout);
                    } finally {
                        IOUtil.close(fin);
                        IOUtil.close(fout);
                    }
                    try {
                        newFile.delete();
                    } catch (Exception e) {
                        newFile.deleteOnExit();
                    }
                } catch (IOException ex) {
                    throw new MojoExecutionException("Could not replace original artifact with uberjar artifact!", ex);
                }
            }
        }
    }

    private File uberjarArtifactFileWithClassifier() {
        Artifact artifact = project.getArtifact();
        String classifier2 = classifier != null ? classifier : "uberjar";
        final String uberjarName = artifact.getArtifactId() + "-" + artifact.getVersion() + "-" + classifier2 + "."
                + artifact.getArtifactHandler().getExtension();
        return new File(outputDirectory, uberjarName);
    }

}
