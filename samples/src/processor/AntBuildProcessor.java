/*
 * $Id: AntBuildProcessor.java,v 1.2.6.1 2007-05-31 22:01:00 ofung Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Processor that runs samples.
 * 
 * @author <ul>
 *         <li>Ryan Shoemaker, Sun Microsystems, Inc.</li>
 *         </ul>
 * @version $Revision: 1.2.6.1 $
 */
public class AntBuildProcessor implements Processor {

    /*
     * (non-Javadoc)
     * 
     * @see processor.Processor#process(java.io.File)
     */
    public boolean process(File dir, boolean verbose) {
        boolean continueProcessing = true;

        // find build.xml
        File buildDotXml =
            new File(dir,"build.xml");

        // buildDotXml should exist, so just attempt to launch it
        continueProcessing = launchAnt(dir, buildDotXml, verbose);

        return continueProcessing;
    }

    /**
     * @param buildDotXml
     * @return
     */
    private boolean launchAnt(File dir, File buildDotXml, boolean verbose) {
        boolean continueProcessing = true;
        File buildDotOut =
            new File(dir,"build.out");

        BuildResultListener buildResultListener = new BuildResultListener();
        
        try {
            trace("launching " + buildDotXml, verbose);

            final Project project = new Project();
            //AntListener listener = new AntListener();
            DefaultLogger listener = new DefaultLogger();
            PrintStream logTo =
                new PrintStream(new FileOutputStream(buildDotOut));
            listener.setOutputPrintStream(logTo);
            listener.setErrorPrintStream(logTo);
            listener.setMessageOutputLevel(Project.MSG_INFO);
            project.addBuildListener(listener);
            project.addBuildListener(buildResultListener);

            Throwable exception = null;
            try {
                project.fireBuildStarted();
                project.init();
                ProjectHelper.configureProject(project, buildDotXml);
                project.executeTarget(project.getDefaultTarget());
            } catch (Throwable t) {
                exception = t;
            } finally {
                project.fireBuildFinished(exception);
            }

            if( buildResultListener.getCause()!=null ) {
                System.out.println("build failed!");
                buildResultListener.getCause().printStackTrace(System.out);
                continueProcessing = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            continueProcessing = false;
        }

        return continueProcessing;
    }

    private void trace(String msg, boolean verbose) {
        if(verbose)
            System.out.println("AntBuildProcessor: " + msg);
    }

    public void addCmdLineOptions(CmdLineParser parser) {
        // no-op
    }
    
    /**
     * {@link BuildListener} that checks if the build
     * was successful or a failure.
     */
    static final class BuildResultListener implements BuildListener {
        
        /**
         * Cause of the failure.
         */
        private Throwable cause;
        
        Throwable getCause() {
            return cause;
        }
        
        public void buildStarted(BuildEvent event) {
            cause = null;
        }
        public void buildFinished(BuildEvent event) {
            cause = event.getException();
        }
        // no-op
        public void targetStarted(BuildEvent event) {
        }
        public void targetFinished(BuildEvent event) {
        }
        public void taskStarted(BuildEvent event) {
        }
        public void taskFinished(BuildEvent event) {
        }
        public void messageLogged(BuildEvent event) {
        }
    }
}
