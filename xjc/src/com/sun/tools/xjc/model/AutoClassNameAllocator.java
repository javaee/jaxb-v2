/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import com.sun.tools.xjc.api.ClassNameAllocator;

/**
 * {@link ClassNameAllocator} filter that performs automatic name conflict resolution.
 * 
 * @author Kohsuke Kawaguchi
 */
public class AutoClassNameAllocator implements ClassNameAllocator {
    private final ClassNameAllocator core;

    private final Map<String,Set<String>> names = new HashMap<String,Set<String>>();

    public AutoClassNameAllocator(ClassNameAllocator core) {
        this.core = core;
    }

    public String assignClassName(String packageName, String className) {
        className = determineName(packageName, className);
        if(core!=null)
            className = core.assignClassName(packageName,className);
        return className;
    }

    private String determineName(String packageName, String className) {
        Set<String> s = names.get(packageName);
        if(s==null) {
            s = new HashSet<String>();
            names.put(packageName,s);
        }

        if(s.add(className))
            return className;

        for(int i=2;true;i++) {
            if(s.add(className+i))
                return className+i;
        }
    }
}
