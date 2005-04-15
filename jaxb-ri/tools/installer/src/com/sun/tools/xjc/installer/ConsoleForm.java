/*
 * $Id: ConsoleForm.java,v 1.1 2005-04-15 20:07:58 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Ryan Shoemaker, Sun Microsystems, Inc.
 * @version $Revision: 1.1 $
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
