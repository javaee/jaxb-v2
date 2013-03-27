/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.tools.jxc;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * Base class for tasks that eventually invoke annotation processing.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ApBasedTask extends Javac {

    /**
     * Implemented by the derived class to set up command line switches passed to annotation processing.
     */
    protected abstract void setupCommandlineSwitches(Commandline cmd);

    private abstract class ApAdapter extends DefaultCompilerAdapter {
        protected ApAdapter() {
            setJavac(ApBasedTask.this);
        }

        protected Commandline setupModernJavacCommandlineSwitches(Commandline cmd) {
            super.setupModernJavacCommandlineSwitches(cmd);
            setupCommandlineSwitches(cmd);
            return cmd;
        }

        protected void logAndAddFilesToCompile(Commandline cmd) {
            attributes.log("Compilation " + cmd.describeArguments(),
                           Project.MSG_VERBOSE);

            StringBuilder niceSourceList = new StringBuilder("File");
            if (compileList.length != 1) {
                niceSourceList.append("s");
            }
            niceSourceList.append(" to be compiled:");

            niceSourceList.append(lSep);

            StringBuilder tempbuilder = new StringBuilder();
            for (File aCompileList : compileList) {
                String arg = aCompileList.getAbsolutePath();
                // cmd.createArgument().setValue(arg); --> we don't need compile list withing cmd arguments
                tempbuilder.append("    ").append(arg).append(lSep);
                niceSourceList.append(tempbuilder);
                tempbuilder.setLength(0);
            }

            attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
        }
    }

    /**
     * Adapter to invoke Ap internally.
     */
    private final class InternalApAdapter extends ApAdapter {

        public boolean execute() throws BuildException {
            try {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                DiagnosticCollector diagnostics = new DiagnosticCollector();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(compileList));
                JavaCompiler.CompilationTask task = compiler.getTask(
                        null,
                        fileManager,
                        diagnostics,
                        Arrays.asList(setupModernJavacCommand().getArguments()),
                        null,
                        compilationUnits);
                task.setProcessors(Collections.singleton(getProcessor()));
                return task.call();
            } catch (BuildException e) {
                throw e;
            } catch (Exception ex) {
                throw new BuildException("Error starting ap", ex, location);
            }
        }
    }

    /**
     * Creates a factory that does the actual job.
     */
    protected abstract Processor getProcessor();

//    /**
//     * Adapter to invoke annotation processing externally.
//     */
//    private final class ExternalAptAdapter extends AptAdapter {
//        public boolean execute() throws BuildException {
//            Commandline cmd = setupModernJavacCommand();
//            return executeExternalCompile(cmd.getArguments(),-1)==0;
//        }
//    }

    protected void compile() {
        if (compileList.length == 0) return;

        log(getCompilationMessage() + compileList.length + " source file"
                + (compileList.length == 1 ? "" : "s"));

        if (listFiles) {
            for (File aCompileList : compileList) {
                String filename = aCompileList.getAbsolutePath();
                log(filename);
            }
        }

        ApAdapter ap = new InternalApAdapter();
//        if(isForkedJavac())
//            ap = new ExternalApAdapter();
//        else

        // compile
        if (!ap.execute()) {
            if (failOnError) {
                throw new BuildException(getFailedMessage(), getLocation());
            } else {
                log(getFailedMessage(), Project.MSG_ERR);
            }
        }
    }

    protected abstract String getCompilationMessage();
    protected abstract String getFailedMessage();
}
