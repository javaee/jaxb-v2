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
package com.sun.tools.xjc.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 */
public class ConsoleForm {

    private final Reader licenseReader;
    private static final int MAX_LENGTH = 70;
    
    private boolean isAccepted = false; 
    
    public ConsoleForm(Reader r) {
        licenseReader = r;
    }

    public void show() throws IOException {
        String license = getLicense();
        System.out.println(license);
        System.out.print("Accept or Decline? [A,D,a,d] ");
        char response = Character.toLowerCase((char)System.in.read());
        if (response == 'a') {
            isAccepted = true;
        }
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }

    private String getLicense() throws IOException {
        BufferedReader reader = new BufferedReader(licenseReader);
        String line;
        StringBuffer buf = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            if( line.length() > MAX_LENGTH ) {
                buf.append(wrapLine(line));
            } else {
                buf.append(line+'\n');
            }
        }
        return buf.toString();
    }

    /**
     * wrap long lines by inserting new line characters.
     */
    private String wrapLine(String line) {
        StringBuffer buf = new StringBuffer();
        int lastSpace;
        String chunk;
        while( line.length() >= MAX_LENGTH ) {
            lastSpace = line.lastIndexOf(' ', MAX_LENGTH);
            chunk = line.substring(0, lastSpace);
            buf.append(chunk+'\n');
            line = line.substring(lastSpace+1, line.length());
        }
        // append the remainder
        buf.append(line+'\n');
        
        return buf.toString();
    }
}
