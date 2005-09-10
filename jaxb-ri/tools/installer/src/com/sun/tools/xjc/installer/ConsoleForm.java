/*
 * $Id: ConsoleForm.java,v 1.2 2005-09-10 19:08:29 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.2 $
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
