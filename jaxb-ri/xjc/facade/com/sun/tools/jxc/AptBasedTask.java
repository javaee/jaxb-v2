package com.sun.tools.jxc;

import java.lang.reflect.Method;

import com.sun.mirror.apt.AnnotationProcessorFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;

/**
 * Base class for tasks that eventually invoke APT.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AptBasedTask extends Javac {

    public AptBasedTask() {
        setExecutable("apt");
    }

    /**
     * Implemented by the derived class to set up command line switches passed to Apt.
     */
    protected abstract void setupCommandlineSwitches(Commandline cmd);

    /**
     * Common parts between {@link InternalAptAdapter} and {@link ExternalAptAdapter}.
     */
    private abstract class AptAdapter extends DefaultCompilerAdapter {
        protected AptAdapter() {
            setJavac(AptBasedTask.this);
        }

        protected Commandline setupModernJavacCommandlineSwitches(Commandline cmd) {
            super.setupModernJavacCommandlineSwitches(cmd);
            setupCommandlineSwitches(cmd);
            return cmd;
        }
    }

    /**
     * Adapter to invoke Apt internally.
     */
    private final class InternalAptAdapter extends AptAdapter {
        public boolean execute() throws BuildException {
            Commandline cmd = setupModernJavacCommand();

            try {
                Class apt = Class.forName ("com.sun.tools.apt.Main");
                Method process;
                try {
                    process = apt.getMethod ("process",
                                        new Class [] {AnnotationProcessorFactory.class,String[].class});
                } catch (NoSuchMethodException e) {
                    throw new BuildException("JDK 1.5.0_01 or later is necessary",e, location);
                }

                int result = ((Integer) process.invoke(null,
                        new Object[] {createFactory(),cmd.getArguments()}))
                    .intValue ();
                return result == 0;
            } catch (BuildException e) {
                throw e;
            } catch (Exception ex) {
                throw new BuildException("Error starting apt",ex, location);
            }
        }
    }

    /**
     * Creates a facotry that does the actual job.
     */
    protected abstract AnnotationProcessorFactory createFactory();

//    /**
//     * Adapter to invoke Apt externally.
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
            for (int i = 0; i < compileList.length; i++) {
                String filename = compileList[i].getAbsolutePath();
                log(filename);
            }
        }

        AptAdapter apt;
//        if(isForkedJavac())
//            apt = new ExternalAptAdapter();
//        else
            apt = new InternalAptAdapter();

        // compile
        if (!apt.execute()) {
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
