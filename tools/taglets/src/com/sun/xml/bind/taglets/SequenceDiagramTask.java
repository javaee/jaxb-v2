package com.sun.xml.bind.taglets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Compiles a sequence diagram into a grpah.
 *
 * @author Kohsuke Kawaguchi
 */
public class SequenceDiagramTask extends MatchingTask {
    private File src;
    private File dst;
    private URL serviceURL;

    public void setSrc(File dir) {
        this.src = dir;
    }

    public void setDest(File dir) {
        this.dst = dir;
    }

    public void setServiceURL(String url) throws MalformedURLException {
        this.serviceURL = new URL(url);
    }

    public void execute() throws BuildException {
        if(serviceURL==null)
            try {
                serviceURL = new URL("http://kohsuke.sfbay.sun.com/sequence-diagram/Build");
            } catch (MalformedURLException e) {
                throw new BuildException(e);
            }

        DirectoryScanner ds = getDirectoryScanner(src);

        try {
            for (String path : ds.getIncludedFiles())
                generate(new File(src,path),new File(dst,path+".png"));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void generate(File in,File out) throws IOException {
        if(!out.getParentFile().exists())
            out.getParentFile().mkdirs();

        if(in.exists() && out.exists() && in.lastModified() < out.lastModified() ) {
            log("Skipping "+in, Project.MSG_INFO);
            return;
        }

        InputStream is = new BufferedInputStream(new FileInputStream(in));
        try {
            log("Generating "+out, Project.MSG_INFO);
            ImageGenerator.generateImage(serviceURL,is,out);
        } finally {
            is.close();
        }
    }

}
