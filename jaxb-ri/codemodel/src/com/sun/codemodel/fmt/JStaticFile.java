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
package com.sun.codemodel.fmt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

/**
 * Allows an application to copy a resource file to the output. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JStaticFile extends JResourceFile {
    
    private final ClassLoader classLoader;
    private final String resourceName;
    private final boolean isResource;

    public JStaticFile(String _resourceName) {
        this(_resourceName,!_resourceName.endsWith(".java"));
    }
    
    public JStaticFile(String _resourceName,boolean isResource) {
        this( JStaticFile.class.getClassLoader(), _resourceName, isResource );
    }

    /**
     * @param isResource
     *      false if this is a Java source file. True if this is other resource files.
     */
    public JStaticFile(ClassLoader _classLoader, String _resourceName, boolean isResource) {
        super(_resourceName.substring(_resourceName.lastIndexOf('/')+1));
        this.classLoader = _classLoader;
        this.resourceName = _resourceName;
        this.isResource = isResource;
    }

    protected boolean isResource() {
        return isResource;
    }

    protected void build(OutputStream os) throws IOException {
        DataInputStream dis = new DataInputStream(classLoader.getResourceAsStream(resourceName));
        
        byte[] buf = new byte[256];
        int sz;
        while( (sz=dis.read(buf))>0 )
            os.write(buf,0,sz);
        
        dis.close();
    }
    
}
