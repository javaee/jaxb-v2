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
