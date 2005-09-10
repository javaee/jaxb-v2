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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a resource file in the application-specific file format.
 */
public abstract class JResourceFile {

    private final String name;
    
    protected JResourceFile( String name ) {
        this.name = name;
    }
    
    /**
     * Gets the name of this property file
     */
    public String name() { 
        return name; 
    }

    /**
     * Returns true if this file should be generated into the directory
     * that the resource files go into.
     *
     * <p>
     * Returns false if this file should be generated into the directory
     * where other source files go.
     */
    protected boolean isResource() {
        return true;
    }

    /**
     * called by JPackage to produce the file image.
     */
    protected abstract void build( OutputStream os ) throws IOException;
}
