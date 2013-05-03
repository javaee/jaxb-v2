/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc.model;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

/**
 * Symbol space for ID/IDREF.
 * 
 * In XJC, the whole ID space is considered to be splitted into
 * one or more "symbol space". For an IDREF to match an ID, we impose
 * additional restriction to the one stated in the XML rec.
 * 
 * <p>
 * That is, XJC'll require that the IDREF belongs to the same symbol
 * space as the ID. Having this concept allows us to assign more
 * specific type to IDREF.
 * 
 * <p>
 * See the design document for detail.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class SymbolSpace
{
    private JType type;
    private final JCodeModel codeModel;
    
    public SymbolSpace( JCodeModel _codeModel ) {
        this.codeModel = _codeModel;
    }
    
    /**
     * Gets the Java type of this symbol space.
     * 
     * <p>
     * A symbol space is said to have a Java type X if all classes
     * pointed by IDs belonging to this symbol space are assignable
     * to X.
     */
    public JType getType() {
        if(type==null)  return codeModel.ref(Object.class);
        return type;
    }
    
    public void setType( JType _type ) {
        if( this.type==null )
            this.type = _type;
    }
    
    public String toString() {
        if(type==null)  return "undetermined";
        else            return type.name();
    }
}
