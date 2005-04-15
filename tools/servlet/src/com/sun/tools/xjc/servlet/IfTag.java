/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import com.sun.xml.bind.webapp.AbstractTagImpl;

/**
 * Custom tag that executes the body only if the servlet is
 * deployed as a part of JWSDP example or as a stand-alone tool. 
 * 
 * <p>
 * See the source code for possible values for the attribute.
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class IfTag extends AbstractTagImpl {

    
    private String test;

    /**
     * @jsp:attribute
     *      required="true"
     *      rtexprvalue="false"
     */
    public void setMode( String test) {
        this.test = test.toUpperCase(); 
    }
    
    public int startTag() {
        if( Mode.inJWSDP && test.equals("JWSDP") )
            return EVAL_BODY_INCLUDE;
        if( !Mode.inJWSDP && test.equals("STANDALONE") )
            return EVAL_BODY_INCLUDE;
        
        if( Mode.canUseDisk && test.equals("USEDISK") )
            return EVAL_BODY_INCLUDE;
        if( !Mode.canUseDisk && test.equals("NODISK") )
            return EVAL_BODY_INCLUDE;
        
        return SKIP_BODY;
    }

    public int endTag() {
        return EVAL_PAGE;
    }

}
