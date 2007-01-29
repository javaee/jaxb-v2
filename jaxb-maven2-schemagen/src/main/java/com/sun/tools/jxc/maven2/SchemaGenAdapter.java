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

import java.io.File;

import com.sun.tools.jxc.SchemaGenTask;
import com.sun.tools.jxc.SchemaGenTask.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
/**
 * Inherit all the Ant SchemaGenTask behaviour and compensate for the missing Ant
 * project and Maven logging facility.
 */
public class SchemaGenAdapter extends SchemaGenTask
{
    /** The logging system. */
    Log log;

    /**
     * Constructor
     *
     * @param log The logging system to use.
     */
    public SchemaGenAdapter(Log log)
    {
        // Logger used when overloading SchemaGenTask log methods
        this.log = log;

        // Create an empty project for the base SchemaGenTask
        setProject(new Project());
    }

    /**
     *  Pass the required value of srcdir.
     */
    public void setSrcdir(File srcdir){
        setSrcdir(new Path(getProject(), srcdir.getAbsolutePath()));
    }
    
    /**
     * Transfer the schema information from the pom to the ant task.
     * Elements namespace and file are mandatory for each Schema.
     *
     * @throws MojoExecutionException
     */
    public void addSchemas(com.sun.tools.jxc.maven2.Schema[] schemas) 
    throws MojoExecutionException {
        
        if (schemas == null)
            return;
        
        //- check of invalid schema definitions.
        for (int i = 0; i < schemas.length; i++ ){
            if (schemas[i].getNamespace() == null || schemas[i].getFile() == null){
                throw new MojoExecutionException(
                "Invalid schema definition in pom file. " +
                "Neither namespace nor file elements may be unset." +
                "\nschema: " + i + 
                "\t namespace = " + schemas[i].getNamespace() + "\tfile = " + schemas[i].getFile());
            }
        }
       
        for (com.sun.tools.jxc.maven2.Schema s : schemas){
            SchemaGenTask.Schema tmpS = null;
            if (s.getNamespace() != null){
                tmpS = createSchema();
                tmpS.setNamespace(s.getNamespace());
            }
            if (s.getFile() != null){
                if (tmpS == null){
                    tmpS = createSchema();
                }
                tmpS.setFile(s.getFile());
            }
        }
    }
    
    /**
     * Overloaded to route logging mesages from SchemaGenTask to the Maven logger.
     *
     * @param message message to log.
     * @param logType message log level.
     */
    public void log(String message, int logType)
    {
        switch (logType)
        {
            case Project.MSG_WARN :
                log.warn(message);
                break;

            case Project.MSG_ERR :
                log.error(message);
                break;

            case Project.MSG_DEBUG :
                log.debug(message);
                break;

            case Project.MSG_VERBOSE :
                // fall to default

            case Project.MSG_INFO :
                // fall to default

            default:
                log.info(message);
        }
    }
}
