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
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.Artifact;
import org.apache.tools.ant.types.Path;
        
import java.io.File;
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
     * A list of regular expression file search patterns to specify the Java files
     * to be processed.  Searching is based from the root of srcdir. If
     * this is not set then all .java files in srcdir will be processed.
     * <p/>
     * @parameter
     */
    protected String[] includes;

    /**
     * A list of regular expression file search patterns to specify the Java files
     * to be excluded from the include list.  Searching is based from the
     * root of srcdir.
     *
     * @parameter
     */
    protected String[] excludes;
    
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
     *
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
   
        // forward file prune list
        schemaGenAdapter.setIncludeExclude(includes, excludes);
        //- Set the (required) path to jaxb-api.jar
        addSchemaGenClasspath(schemaGenAdapter);
        addUserDefinedJars(schemaGenAdapter);
        //-checkArtifacts(); //- debug
        //- forward all schema def to the processing task
        schemaGenAdapter.addSchemas(schemas);
        schemaGenAdapter.execute();
    }
    
    /**
     * SchemaGenTask requires the jaxb-api.jar ref to resolve annotation references
     * when the internal utility compiles the java files.  This utility can not proceed
     * successfully without it.  The plugin already references this jar so take
     * it from therem
     * 
     * @throws MojoExecutionException
     */
    private void addSchemaGenClasspath(SchemaGenAdapter schemaGenAdapter)throws MojoExecutionException{        
        Iterator itl =  pluginArtifacts.iterator();
        while(itl.hasNext()){
            DefaultArtifact df = (DefaultArtifact)itl.next();
            String tmpName = df.getGroupId() + ":" + df.getArtifactId();
            if (tmpName.equals(JAXB_API_JAR_IDENTIFIER)){
                String tmpS = df.getFile().getAbsolutePath();
                getLog().info("jaxb-schemagen classpath addition: " + tmpS);
                schemaGenAdapter.setClasspath(new Path(schemaGenAdapter.getProject(), tmpS));
                return;
            }
        }
        getLog().info("jaxb-api.jar reference not found in maven-jaxb-schemagen-plugin artifacts");
        throw new MojoExecutionException("jaxb-api.jar is required and was not found.");
    }
    
    /**
     * Add any user specified jars to the internal classpath.
     * These are JARs that are needed to resolve references in the users java files.
     * Dependencies must have been set at the project level.
     *      <project>
     *         <dependencies>
     *            <dependency>
     *            </dependency>
     *         </dependencies>
     *      <project>
     */
    private void addUserDefinedJars(SchemaGenAdapter schemaGenAdapter){
        for (Artifact df : (Set<Artifact>)project.getArtifacts()) {
            if (df.getFile() == null) continue; // skip
            getLog().info("jaxb-schemagen classpath addition: " + df.getFile().getAbsolutePath());
            schemaGenAdapter.setClasspath(new Path(schemaGenAdapter.getProject(), df.getFile().getAbsolutePath()));
        }
    }
    
    //-- debugging only
    private void checkArtifacts(){
        Set tmpS = project.getArtifacts();
        Set tmpP = project.getPluginArtifacts();
        Set tmpDa = project.getDependencyArtifacts(); // returns all project/dependencies in the pom
        Set tmpEx = project.getExtensionArtifacts();
        Iterator itl =  tmpS.iterator();
        while(itl.hasNext()){
            DefaultArtifact df = (DefaultArtifact)itl.next();
            //-System.out.println("checkArtifacts: tmpS: " + itl.next().getClass().getName());
            System.out.println("checkArtifacts: tmpS: " + df);
        }
        itl =  tmpP.iterator();
        while(itl.hasNext()){
            DefaultArtifact df = (DefaultArtifact)itl.next();
            //-System.out.println("checkArtifacts: tmpP: " + itl.next().getClass().getName());
            System.out.println("checkArtifacts: tmpP: " + df);
/***            
            List trailList = df.getDependencyTrail();
            itl =  trailList.iterator();
            while(itl.hasNext()){
                System.out.println("addSchemaGenClasspath:depTrail: " + ((String)itl.next()).toString());
            }
 ***/
        }
        itl =  tmpDa.iterator();
        while(itl.hasNext()){
            DefaultArtifact df = (DefaultArtifact)itl.next();
            //-System.out.println("checkArtifacts: tmpP: " + itl.next().getClass().getName());
            System.out.println("checkArtifacts: tmpDa: " + df.getFile().toString());
        }
        itl =  tmpEx.iterator();
        while(itl.hasNext()){
            DefaultArtifact df = (DefaultArtifact)itl.next();
            //-System.out.println("checkArtifacts: tmpP: " + itl.next().getClass().getName());
            System.out.println("checkArtifacts: tmpEx: " + df);
        }
        try {
            List tmpRt = project.getRuntimeClasspathElements();
            itl =  tmpRt.iterator();
            while(itl.hasNext()){
                //System.out.println("checkArtifacts: tmpRt: " + itl.next().getClass().getName());
                System.out.println("checkArtifacts: tmpRt: " + ((String)itl.next()));
            }
        } catch(org.apache.maven.artifact.DependencyResolutionRequiredException e){
            System.out.println("checkArtifacts: tmpRt: " + e);
        }
        
        
        /***************
        List tmpB = project.getBuildPlugins();
        itl =  tmpB.iterator();
        while(itl.hasNext()){
             org.apache.maven.model.Plugin p = (org.apache.maven.model.Plugin)itl.next();
             //System.out.println("checkArtifacts: P: " + p.getClass().getName());
            //java.lang.reflect.Method[] mlist = p.getClass().getMethods();
            //for (java.lang.reflect.Method m : mlist){
            //   System.out.println("checkArtifacts: method: " + m); 
            //}
            List dList = p.getDependencies();
            Iterator jtl =  dList.iterator();
            while(jtl.hasNext()){
                org.apache.maven.model.Dependency tmpDep =
                        (org.apache.maven.model.Dependency)jtl.next();
                java.lang.reflect.Method[] deplist = tmpDep.getClass().getMethods();
                System.out.println("checkArtifacts: Dependencies: " + tmpDep.getClass().getName());
                for (java.lang.reflect.Method m : deplist){
                    System.out.println("checkArtifacts: Dependencies: method: " + m); 
                }
            }
            jtl =  dList.iterator();
            while(jtl.hasNext()){
                org.apache.maven.model.Dependency tmpDep =
                        (org.apache.maven.model.Dependency)jtl.next();
                System.out.println("checkArtifacts: Dependencies: " + tmpDep.toString());
                System.out.println("checkArtifacts: Dependencies: classpath: " + tmpDep.getSystemPath());
            }
            //System.out.println("checkArtifacts: tmpP: " + df);
        }
        *************/
    }
 
    /**
     * Log the configuration settings.  Shown when exception thrown or when
     * verbose is true.
     */
    private void logSettings() {
        getLog().info("srcdir: " + srcdir);
        getLog().info("destdir: " + destdir);
        if (includes != null){
           for(String s : includes)
                getLog().info("include: " + s); 
        }
        if (excludes != null){
           for(String s : excludes)
                getLog().info("exclude: " + s); 
        }
        if (schemas != null){
            for(Schema tmpS : schemas)
                getLog().info("schema: " + tmpS);
        }
    } 
}
