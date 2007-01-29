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
package com.sun.tools.jxc.maven2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.Path;
import org.apache.maven.artifact.DefaultArtifact;
        
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

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
 * @requiresDependencyResolution runtime
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
    protected File destdir;
    
    /**
     * By default this utility uses the jaxb-api.jar defined in the
     * maven-jaxb-schemagen-plugin POM.xml file.  The user can over ride
     * that reference by providing the path name to the jaxb-api.jar file
     * preferred (e.g.  ../local/lib/jaxb-api.jar).
     *
     * Jar file that contains the Jaxb annotations. 
     *
     * @parameter expression=null
     */
    protected String jaxbApiJar;
    
    /**
     *  groupId:artifactId of needed jar file.
     */
    private final String JAXB_API_JAR_IDENTIFIER = "javax.xml.bind:jaxb-api";
    
    
    /**
     * The dependencies list of the jaxb-maven2-schemagen plugin.
     * This class checks this list for a needed jar file.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
     private List pluginArtifacts;

    /**
     * If verbose all the configured settings that are to be passed to schemagen
     * are logged.
     *
     * @parameter expression="false"
     */
    protected boolean verbose;
    
    /**
     * Control the file name of the generated schema.
     * Elements namespace and file are mandatory.
     *  <schemas>
     *     <schema>
     *        <namespace>some.name</namespace>
     *        <file>schemafilename.xsd</file>
     *     </schema>
     *  </schemas>
     * @parameter
     */
    private Schema[] schemas;
    
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
        if (destdir != null && !destdir.exists()) {
            destdir.mkdirs();
        }
        
        // Create the schemagen adapter
        SchemaGenAdapter schemaGenAdapter = new SchemaGenAdapter(getLog());
   
        if (destdir != null) {
            schemaGenAdapter.setDestdir(destdir);
        }
        schemaGenAdapter.setSrcdir(srcdir);
   
        if (project != null) {
            project.addCompileSourceRoot(destdir.getPath());
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
            
            //-Pass dependency jar list to schema task
            schemaGenAdapter.addConfiguredDepends(pomDependencies);
       
            //- Set the (required) path to jaxb-api.jar
            addSchemaGenClasspath(schemaGenAdapter);
        }
        // Configure production artifacts to determine generation
        FileSet products = new FileSet();
        File outDir = destdir;
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
        //- forward all schema def to the processing task
        schemaGenAdapter.addSchemas(schemas);
        
        schemaGenAdapter.execute();
    }
    
    /**
     * SchemaGenTask requires the jaxb-api.jar ref to resolve annotation references
     * when the internal utility compiles the java files.  This utility can not proceed
     * successfully without it.  
     * 
     * There is a search hierarchy to resolve the reference. The user can override the 
     * default reference by setting element <jaxbApiJar> to the jar file to be used; 
     * this value is checked first. The default reference in the plugin is checked last.
     */
    private void addSchemaGenClasspath(SchemaGenAdapter schemaGenAdapter)throws MojoExecutionException{
        File tmpJar = null;
        
        //- Check for user override.  (Maven passing string null when value not set.)
        if (!jaxbApiJar.equals("null")){
            File tmpF = new File(jaxbApiJar);
            if (!tmpF.getName().endsWith(".jar") || !tmpF.exists() || !tmpF.isFile()){
                getLog().info("jaxbApiJar: " + jaxbApiJar + " jar file is not found.");
                getLog().info("Checking maven-jaxb-schemagen-plugin artifacts for jaxb-api.jar reference");
            } else {
                tmpJar = tmpF;
            }
        }         
        
        //- check the plugin's dependencies for a reference to the needed jar file.
        if (tmpJar == null){
            Iterator itl =  pluginArtifacts.iterator();
            while(itl.hasNext()){
                DefaultArtifact df = (DefaultArtifact)itl.next();
                String tmpName = df.getGroupId() + ":" + df.getArtifactId();
                if (tmpName.equals(JAXB_API_JAR_IDENTIFIER)){
                    tmpJar = df.getFile();
                    break;
                }
            }
            if (tmpJar == null)
                getLog().info("jaxb-api.jar reference not found in maven-jaxb-schemagen-plugin artifacts");
        }
        if (tmpJar == null){
            throw new MojoExecutionException(
                "jaxb-api.jar is required and was not found.");
        }
        getLog().info("jaxb-schemagen classpath addition: " + tmpJar.getAbsolutePath());
        schemaGenAdapter.setClasspath(new Path(schemaGenAdapter.getProject(), tmpJar.getAbsolutePath()));
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
        getLog().info("jaxbApiJar: " + jaxbApiJar);
        getLog().info("destdir: " + destdir);
        if (schemas != null){
            for(Schema tmpS : schemas)
                getLog().info("schema: " + tmpS);
        }
    }
}
