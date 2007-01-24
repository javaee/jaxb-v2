/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.maven2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

import java.io.File;
import java.util.Collection;

/**
 * A mojo that generates XML schema from Java files. For
 * details on JAXB see <a href="https://jaxb.dev.java.net/">JAXB 2.0
 * Project</a>
 * <p/>
 *
 * @author Rebecca Searls (rebecca.searls@sun.com)
 * @goal generate
 * @phase generate-sources
 * @description JAXB Java files to XML schema from generator plugin
 */
public class SchemaGenMojo extends AbstractMojo {
    /**
     * The source directory containing java files.
     *
     * @parameter expression="${project.build.directory}/../src/main"
     */
    protected File srcdir;
    
    /**
     * Generated schema files will be written under this directory. 
     *
     * @parameter expression="${project.build.directory}/generated-schema"
     */
    protected File generateDirectory;
    
    /**
     * If verbose all the configured settings that are to be passed to schemagen
     * are logged.
     *
     * @parameter expression="false"
     */
    protected boolean verbose;
    
    
    /**
     * The Maven project reference.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;
    
    /**
     * Execute the maven2 mojo to invoke schemagen based on configuration
     * settings.
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        if (verbose) {
            logSettings();
        }
        
        // Create the destination path if it does not exist.
        if (generateDirectory != null && !generateDirectory.exists()) {
            generateDirectory.mkdirs();
        }
        
        // Create the schemagen adapter
        SchemaGenAdapter schemaGenAdapter = new SchemaGenAdapter(getLog());
   
        if (generateDirectory != null) {
            //-xjc2TaskAdapter.setDestdir(generateDirectory);
            schemaGenAdapter.setDestdir(generateDirectory);
        }
        schemaGenAdapter.setSrcdir(srcdir);
   
        if (project != null) {
            project.addCompileSourceRoot(generateDirectory.getPath());
        }
     
        // Pom dependency - typically when configuration settings change.
        if (project != null) {
            FileSet pomDependencies = new FileSet();
            pomDependencies.setDir(project.getFile().getParentFile());
            
            if (verbose) {
                getLog().info("pom dependency: " + project.getFile().getPath());
            }
            pomDependencies.addFilename(
                    createFilenameSelector(project.getFile().getName()));
            
            //-method appears to be xjc specific; may need to remove block
            //-schemaGenAdapter.addConfiguredDepends(pomDependencies);
        }
        
        // Configure production artifacts to determine generation
        FileSet products = new FileSet();
        File outDir = generateDirectory;
        products.setDir(outDir);
        products.setIncludes("**/*.java");
        //method appears to be xjc specific; may need to remove block
        //-schemaGenAdapter.addConfiguredProduces(products);
/**   unclear if this is to remain; SchemaGenTask would need to be changed     
        if (args != null) {
            schemaGenAdapter.createArg().setLine(args);
            //xjc2TaskAdapter.createArg().setLine(args);
        }
***/        

        schemaGenAdapter.execute();
    }
    
    /**
     * Create a FilenameSelector from a filename.
     *
     * @param filename filename to wrap in FilenameSelector.
     * @return the FilenameSelector based on the given filename.
     */
    private static FilenameSelector createFilenameSelector(String filename) {
        FilenameSelector selector = new FilenameSelector();
        selector.setName(filename);
        return selector;
    }
    
    /**
     * Log the configuration settings.  Shown when exception thrown or when
     * verbose is true.
     */
    private void logSettings() {
        getLog().info("srcdir: " + srcdir);
        getLog().info("generateDirectory: " + generateDirectory);        
    }
}
