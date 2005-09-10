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
