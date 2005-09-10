/*
 * $Id: AntBuildProcessor.java,v 1.2 2005-09-10 19:08:22 kohsuke Exp $
 */

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
 * @version $Revision: 1.2 $
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
