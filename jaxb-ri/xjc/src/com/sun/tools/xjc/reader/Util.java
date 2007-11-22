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
package com.sun.tools.xjc.reader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.InputSource;

public class Util
{
    /**
     * Parses the specified string either as an {@link URL} or as a {@link File}.
     *
     * @throws IOException
     *      if the parameter is neither.
     */
    public static Object getFileOrURL(String fileOrURL) throws IOException {
        try {
            return new URL(fileOrURL);
        } catch (MalformedURLException e) {
            return new File(fileOrURL).getCanonicalFile();
        }
    }
    /**
     * Gets an InputSource from a string, which contains either
     * a file name or an URL.
     */
    public static InputSource getInputSource(String fileOrURL) {
        try {
            Object o = getFileOrURL(fileOrURL);
            if(o instanceof URL) {
                return new InputSource(escapeSpace(((URL)o).toExternalForm()));
            } else {
                String url = ((File)o).toURL().toExternalForm();
                return new InputSource(escapeSpace(url));
            }
        } catch (IOException e) {
            return new InputSource(fileOrURL);
        }
    }

    public static String escapeSpace( String url ) {
        // URLEncoder didn't work.
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            // TODO: not sure if this is the only character that needs to be escaped.
            if (url.charAt(i) == ' ')
                buf.append("%20");
            else
                buf.append(url.charAt(i));
        }
        return buf.toString();
    }
}
