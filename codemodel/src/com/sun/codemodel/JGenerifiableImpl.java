/*
 * @(#)$Id: JGenerifiableImpl.java,v 1.1 2005-04-15 20:02:51 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link JGenerifiable}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class JGenerifiableImpl implements JGenerifiable, JDeclaration {
    
    /** Lazily created list of {@link JTypeVar}s. */
    private List typeVariables = null;
    
    protected abstract JCodeModel owner();
    
    public void declare( JFormatter f ) {
        if(typeVariables!=null) {
            f.p('<');
            for (int i = 0; i < typeVariables.size(); i++) {
                JTypeVar v = (JTypeVar)typeVariables.get(i);
                if(i!=0)    f.p(',');
                f.d(v);
            }
            f.p('>');
        }
    }


    public JTypeVar generify(String name) {
        JTypeVar v = new JTypeVar(owner(),name);
        if(typeVariables==null)
            typeVariables = new ArrayList(3);
        typeVariables.add(v);
        return v;
    }

    public JTypeVar generify(String name, Class bound) {
        return generify(name,owner().ref(bound));
    }

    public JTypeVar generify(String name, JClass bound) {
        return generify(name).bound(bound);
    }
    
    public JTypeVar[] typeParams() {
        if(typeVariables==null)
            return JTypeVar.EMPTY_ARRAY;
        else
            return (JTypeVar[]) typeVariables.toArray(new JTypeVar[typeVariables.size()]);
    }

}
