package com.sun.tools.xjc.installer.builder;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task that builds installer package.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class BuilderTask extends Task {
    private File licenseFile;
    private File zipFile;
    private File classFile;

    public void setClassFile(File classFile) {
        this.classFile = classFile;
    }

    public void setJarFile(File jarFile) {
        this.classFile = jarFile;
    }

    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void execute() throws BuildException {
        // TODO: up-to-date check

        log("Building "+classFile,Project.MSG_INFO);

        try {
            Main.build(licenseFile,zipFile,classFile);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }


}
