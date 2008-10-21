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

package com.sun.tools.xjc.reader.relaxng;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.xml.bind.v2.model.core.ID;

import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

import static com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT;

/**
 * Recursively visits {@link DPattern} and
 * decides which patterns to map to properties.
 *
 * @author Kohsuke Kawaguchi
 */
final class ContentModelBinder extends DPatternWalker {
    private final RELAXNGCompiler compiler;
    private final CClassInfo clazz;

    private boolean insideOptional = false;
    private int iota=1;

    public ContentModelBinder(RELAXNGCompiler compiler,CClassInfo clazz) {
        this.compiler = compiler;
        this.clazz = clazz;
    }

    public Void onMixed(DMixedPattern p) {
        throw new UnsupportedOperationException();
    }

    public Void onChoice(DChoicePattern p) {
        boolean old = insideOptional;
        insideOptional = true;
        super.onChoice(p);
        insideOptional = old;
        return null;
    }

    public Void onOptional(DOptionalPattern p) {
        boolean old = insideOptional;
        insideOptional = true;
        super.onOptional(p);
        insideOptional = old;
        return null;
    }

    public Void onZeroOrMore(DZeroOrMorePattern p) {
        return onRepeated(p,true);
    }

    public Void onOneOrMore(DOneOrMorePattern p) {
        return onRepeated(p,insideOptional);

    }

    private Void onRepeated(DPattern p,boolean optional) {
        RawTypeSet rts = RawTypeSetBuilder.build(compiler, p, optional? Multiplicity.STAR : Multiplicity.PLUS);
        if(rts.canBeTypeRefs==RawTypeSet.Mode.SHOULD_BE_TYPEREF) {
            CElementPropertyInfo prop = new CElementPropertyInfo(
                    calcName(p),REPEATED_ELEMENT,ID.NONE,null,null,null,p.getLocation(),!optional);
            rts.addTo(prop);
            clazz.addProperty(prop);
        } else {
            CReferencePropertyInfo prop = new CReferencePropertyInfo(
                    calcName(p),true,!optional,false/*TODO*/,null,null,p.getLocation(), false, false);
            rts.addTo(prop);
            clazz.addProperty(prop);
        }

        return null;
    }

    public Void onAttribute(DAttributePattern p) {
        // TODO: support multiple names
        QName name = p.getName().listNames().iterator().next();

        CAttributePropertyInfo ap = new CAttributePropertyInfo(
           calcName(p), null,null/*TODO*/, p.getLocation(), name,
                p.getChild().accept(compiler.typeUseBinder), null,
                !insideOptional);
        clazz.addProperty(ap);

        return null;
    }

    private String calcName(DPattern p) {
        // TODO
        return "field"+(iota++);
    }
}
