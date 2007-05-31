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

package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.xml.bind.v2.ContextFactory;

/**
 * {@link ObjectFactoryGenerator} used when we generate
 * interfaces and implementations in separate packages.
 *
 * <p>
 * {@link #publicOFG} and {@link #privateOFG} gives you access to
 * {@code ObjectFactory}s in both packages, if you need to.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DualObjectFactoryGenerator extends ObjectFactoryGenerator {
    public final ObjectFactoryGenerator publicOFG;
    public final ObjectFactoryGenerator privateOFG;

    DualObjectFactoryGenerator(BeanGenerator outline, Model model, JPackage targetPackage) {
        this.publicOFG = new PublicObjectFactoryGenerator(outline,model,targetPackage);
        this.privateOFG = new PrivateObjectFactoryGenerator(outline,model,targetPackage);

        // put the marker so that we can detect missing jaxb.properties
        publicOFG.getObjectFactory().field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
                Void.class, ContextFactory.USE_JAXB_PROPERTIES, JExpr._null());
    }

    void populate(CElementInfo ei) {
        publicOFG.populate(ei);
        privateOFG.populate(ei);
    }

    void populate(ClassOutlineImpl cc) {
        publicOFG.populate(cc);
        privateOFG.populate(cc);
    }

    /**
     * Returns the private version (which is what gets used at runtime.)
     */
    public JDefinedClass getObjectFactory() {
        return privateOFG.getObjectFactory();
    }
}
