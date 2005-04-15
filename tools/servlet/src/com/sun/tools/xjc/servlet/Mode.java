/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.util.ResourceBundle;

/**
 * Defines the various constants the dictates the behavior of the servlet.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Mode {
    
    /**
     * True if this is a part of the JWSDP example.
     */
    public static boolean inJWSDP; // TODO: look up this value from somewhere
    
    static {
        String mode = ResourceBundle.getBundle(Mode.class.getName()).getString("mode");
        inJWSDP = mode.equals("jwsdp");
    }

    /**
     * True to enable featuers that use disk space.
     */
    public static boolean canUseDisk = true;
    
    /**
     * Usage notification e-mail will be sent to this address.
     */
    public static String homeAddress = "jaxb-dev@sun.com";
    
    /**
     * SMTP server that can receive e-mails to the above address.
     */
    public static String mailServer = "mail.sun.net";
}
