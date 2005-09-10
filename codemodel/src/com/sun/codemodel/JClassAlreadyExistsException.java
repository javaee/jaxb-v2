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
package com.sun.codemodel;

/**
 * Indicates that the class is already created.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JClassAlreadyExistsException extends Exception {
    private final JDefinedClass existing;
    
    public JClassAlreadyExistsException( JDefinedClass _existing ) {
        this.existing = _existing;
    }
    
    /**
     * Gets a reference to the existing {@link JDefinedClass}.
     * 
     * @return
     *      This method always return non-null valid object.
     */
    public JDefinedClass getExistingClass() {
        return existing;
    }
}
