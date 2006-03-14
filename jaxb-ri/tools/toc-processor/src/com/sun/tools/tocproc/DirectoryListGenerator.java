package com.sun.tools.tocproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Generates a simple XML file listing files in a directory.
 *
 * @author Kohsuke Kawaguchi
 */
public class DirectoryListGenerator extends Task {

    /**
     * Directory to generate a list.
     */
    private File dir;

    /**
     * File to be written.
     */
    private File dest;

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void execute() throws BuildException {
        String[] subdirs = dir.list();

        try {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest),"UTF-8"));

            w.println("<directory name='"+dir+"'>");
            for (String sd : subdirs) {
                w.println(String.format("  <file name='%1s' type='%2s' url='%3s'/>",sd,
                    new File(dir,sd).isDirectory()?"dir":"file",
                    new File(dir,sd).getAbsoluteFile().toURL()));
            }
            w.println("</directory>");
            w.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
