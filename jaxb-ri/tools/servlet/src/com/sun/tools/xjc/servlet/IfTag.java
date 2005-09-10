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
