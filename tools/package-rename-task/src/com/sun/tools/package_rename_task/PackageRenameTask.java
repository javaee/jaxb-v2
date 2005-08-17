package com.sun.tools.package_rename_task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Ant task that renames FQCN references in source files.
 *
 * Used to avoid jarhell.
 *
 * @author Kohsuke Kawaguchi
 */
public class PackageRenameTask extends MatchingTask {
    public PackageRenameTask() {
        setTaskName("package-rename");
    }

    private File srcDir;

    /**
     * Destination directory
     */
    private File destDir;

    /**
     * The prefix to be replaced.
     *
     * For example, if from="com.sun." and to="org.jvnet.",
     * "com.sun.xml.bind" gets renamed to "org.jvnet.xml.bind".
     */
    private String from;
    /**
     * The new prefix.
     */
    private String to;

    private byte[] buffer = new byte[8192];

    public void setSrcdir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void execute() throws BuildException {
        if(srcDir==null)
            throw new BuildException("@srcdir isn't specified");
        if(destDir==null)
            throw new BuildException("@destdir isn't specified");
        if(from==null)
            throw new BuildException("@from isn't specified");
        if(to==null)
            throw new BuildException("@to isn't specified");

        DirectoryScanner ds = super.getDirectoryScanner(srcDir);
        String[] includedFiles = ds.getIncludedFiles();

        int count = 0;

        for( int i=0; i<includedFiles.length; i++ ) {
            if(process(includedFiles[i].replace('\\','/')))
                count++;
        }

        log("Processed "+count+" file"+(count!=1?"s":""),Project.MSG_INFO);
    }

    /**
     *
     * @return true
     *      if the file is overwritten.
     */
    private boolean process(String relPath) {
        File src = new File(srcDir, relPath);
        File dst = new File(destDir,replace(relPath));
        log("Processing "+src,Project.MSG_VERBOSE);

        // up to date check
        if(dst.exists() && dst.lastModified() > src.lastModified()) {
            log("Skipping: up-to-date",Project.MSG_VERBOSE);
            return false;
        }

        // create directories
        File dd = dst.getParentFile();
        if(!dd.exists()) {
            if(!dd.mkdirs())
                throw new BuildException("failed to mkdir "+dd);
        }

        FileInputStream fis=null;
        FileOutputStream fos=null;

        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dst);

            // just copy non-Java files
            if(!src.getName().endsWith(".java")) {
                copy(fis,fos);
                return true;
            }

            processJava(
                new BufferedReader(new InputStreamReader(fis)),
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos))));
            return true;
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            if(fis!=null)
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            if(fos!=null)
                try {
                    fos.close();
                } catch (IOException e) {
                    // ignore
                }
            // avoid careless users from modifying them
            // because those changes will be lost.
            dst.setReadOnly();
        }
    }

    private void processJava(BufferedReader r, PrintWriter w) throws IOException {
        String line;

        while(true) {
            line = r.readLine();
            if(line==null) {
                r.close();
                w.close();
                return;
            }

            String meat = line.trim();

            if(meat.startsWith("package ")) {
                String pkgName = meat.substring(7).trim();
                line = "package "+replace(pkgName);
            }
            else
            if(meat.startsWith("import static ")) {
                String pkgName = meat.substring(14).trim();
                line = "import static "+replace(pkgName);
            }
            else
            if(meat.startsWith("import ")) {
                String pkgName = meat.substring(7).trim();
                line = "import "+replace(pkgName);
            }

            w.println(line);
        }
    }

    /**
     * Replaces the typeName's prefix if applicable
     */
    private String replace(String typeName) {
        if(typeName.startsWith(from)) {
            String tail = typeName.substring(from.length());
            if(tail.length()==0 || !Character.isJavaIdentifierPart(tail.charAt(0)))
                return to+tail;
        }
        if(typeName.startsWith(from.replace('.','/'))) {
            String tail = typeName.substring(from.length());
            if(tail.length()==0 || !Character.isJavaIdentifierPart(tail.charAt(0)))
                return to.replace('.','/')+tail;
        }

        return typeName;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        int len;
        while((len=in.read(buffer))>=0)
            out.write(buffer,0,len);
    }
}
