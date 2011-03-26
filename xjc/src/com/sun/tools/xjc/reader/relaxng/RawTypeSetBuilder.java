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

package com.sun.tools.xjc.reader.relaxng;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.xml.bind.v2.model.core.ID;

import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

/**
 * Builds {@link RawTypeSet} for RELAX NG.
 *
 * @author Kohsuke Kawaguchi
 */
public final class RawTypeSetBuilder extends DPatternWalker {
    public static RawTypeSet build( RELAXNGCompiler compiler, DPattern contentModel, Multiplicity mul ) {
        RawTypeSetBuilder builder = new RawTypeSetBuilder(compiler,mul);
        contentModel.accept(builder);
        return builder.create();
    }

    /**
     * Multiplicity of the property.
     */
    private Multiplicity mul;

    /**
     * Accumulates discovered {@link RawTypeSet.Ref}s.
     */
    private final Set<RawTypeSet.Ref> refs = new HashSet<RawTypeSet.Ref>();

    private final RELAXNGCompiler compiler;

    public RawTypeSetBuilder(RELAXNGCompiler compiler,Multiplicity mul) {
        this.mul = mul;
        this.compiler = compiler;
    }

    private RawTypeSet create() {
        return new RawTypeSet(refs,mul);
    }

    public Void onAttribute(DAttributePattern p) {
        // attributes form their own properties
        return null;
    }

    public Void onElement(DElementPattern p) {
        CTypeInfo[] tis = compiler.classes.get(p);
        if(tis!=null) {
            for( CTypeInfo ti : tis )
                refs.add(new CClassInfoRef((CClassInfo)ti));
        } else {
            // TODO
            assert false;
        }
        return null;
    }

    public Void onZeroOrMore(DZeroOrMorePattern p) {
        mul = mul.makeRepeated();
        return super.onZeroOrMore(p);
    }

    public Void onOneOrMore(DOneOrMorePattern p) {
        mul = mul.makeRepeated();
        return super.onOneOrMore(p);
    }

    /**
     * For {@link CClassInfo}s that map to elements.
     */
    private static final class CClassInfoRef extends RawTypeSet.Ref {
        private final CClassInfo ci;
        CClassInfoRef(CClassInfo ci) {
            this.ci = ci;
            assert ci.isElement();
        }

        protected ID id() {
            return ID.NONE;
        }

        protected boolean isListOfValues() {
            return false;
        }

        protected RawTypeSet.Mode canBeType(RawTypeSet parent) {
            return RawTypeSet.Mode.SHOULD_BE_TYPEREF;
        }

        protected void toElementRef(CReferencePropertyInfo prop) {
            prop.getElements().add(ci);
        }

        protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
            return new CTypeRef(ci,ci.getElementName(),ci.getTypeName(),false,null);
        }
    }
}
