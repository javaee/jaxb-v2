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

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
final class ImageGenerator {
    /**
     * Generates an image.
     */
    public static void generateImage(URL serviceURL, String contents, File out) throws IOException {
        generateImage(serviceURL,new ByteArrayInputStream(contents.getBytes()),out);
    }

    /**
     * Generates an image.
     */
    public static void generateImage(URL serviceURL, InputStream in, File out) throws IOException {
        HttpURLConnection con = (HttpURLConnection)serviceURL.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.connect();
        copy(in,con.getOutputStream());
        con.getOutputStream().close();
        OutputStream os = new FileOutputStream(out);
        if(con.getResponseCode()>=300) {
            ByteArrayOutputStream w = new ByteArrayOutputStream();
            copy(con.getErrorStream(),w);
            throw new Error(new String(w.toByteArray()));
        }
        copy(con.getInputStream(),os);
        con.getInputStream().close();
        os.close();
    }

    private static void copy(InputStream r, OutputStream w) throws IOException {
        byte[] buf = new byte[256];
        int len;

        while((len=r.read(buf))>=0)
            w.write(buf,0,len);
    }
}
