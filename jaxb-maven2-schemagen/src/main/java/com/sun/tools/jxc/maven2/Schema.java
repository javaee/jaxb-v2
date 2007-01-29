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
package com.sun.tools.jxc.maven2;

import java.io.File;

/*
 *  Meeting maven requirements defining complex (configuration)
 *  objects.  The corresponding (mapping) class must reside in 
 *  same dir as the mojo.  This information will be mapped to the
 *  Schema class in the SchemaGenTask.
 */
public class Schema {
    /*
     * @required
     * @parameter
     */
    private String namespace;
    
    /*
     * @required
     * @parameter
     */
    private String file;
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public String getNamespace() {
        return this.namespace;
    }
    public void setFile(String fileName) {
        this.file = fileName;
    }
    public String getFile() {
        return this.file;
    }
    public String toString(){
        return "namespace=" + this.namespace + "\tfile=" + this.file;
    }
}