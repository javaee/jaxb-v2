/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that adds licence header to java source files.
 *
 * @author Martin Grebac (martin.grebac@oracle.com)
 */
public class LicenceTask extends Task {

    private File licence = null;

    private Vector filesets = new Vector();
    
    public LicenceTask() {
    }

    public void setLicence(File licence) {
        this.licence = licence;
    }
    
    @Override
    public void setProject(Project project) {
        super.setProject(project);
    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    @Override
    public void execute() throws BuildException {
        
        Iterator iter = filesets.iterator();
        while (iter.hasNext()) {
            FileSet fset = (FileSet) iter.next();
            DirectoryScanner ds = fset.getDirectoryScanner(project);
            File dir = ds.getBasedir();
            String[] filesInSet = ds.getIncludedFiles();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + dir);
            for (String filename : filesInSet) {
                System.out.println(filename);
                File file = new File(dir,filename);

                String licenceHeader = readFile(licence);
                String javafile = readFile(file);

                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(file);
                pw.write(licenceHeader);
                pw.write(javafile);

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LicenceTask.class.getName()).log(Level.SEVERE, null, ex);
                } finally { 
                    if (pw != null) {
                        pw.close();
                    }
                }
            }
        }
    }

    private String readFile(File file) {
        String licenceHeader = null;
        LineNumberReader lnr = null;
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            lnr = new LineNumberReader(fr);
            licenceHeader = lnr.readLine();
            do {
                licenceHeader += "\n";
                String line = lnr.readLine();
                if (line == null) {
                    break;
                } else {
                    licenceHeader += line;
                }
            } while (true);
        } catch (IOException ex) {
            if (lnr != null) {
                try {
                    lnr.close();
                } catch (IOException ex1) {
                    Logger.getLogger(LicenceTask.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex1) {
                    Logger.getLogger(LicenceTask.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            Logger.getLogger(LicenceTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return licenceHeader;
    }
    
}
