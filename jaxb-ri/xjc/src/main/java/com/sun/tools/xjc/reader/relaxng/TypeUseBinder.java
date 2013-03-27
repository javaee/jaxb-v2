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

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;

import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DContainerPattern;
import org.kohsuke.rngom.digested.DDataPattern;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DEmptyPattern;
import org.kohsuke.rngom.digested.DGrammarPattern;
import org.kohsuke.rngom.digested.DGroupPattern;
import org.kohsuke.rngom.digested.DInterleavePattern;
import org.kohsuke.rngom.digested.DListPattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DNotAllowedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternVisitor;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DTextPattern;
import org.kohsuke.rngom.digested.DValuePattern;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

/**
 * Walks the pattern tree and binds it to a {@link TypeUse}.
 *
 * The singleton instance is kept in {@link RELAXNGCompiler}.
 *
 * TODO: I should really normalize before process.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypeUseBinder implements DPatternVisitor<TypeUse> {
    private final RELAXNGCompiler compiler;

    public TypeUseBinder(RELAXNGCompiler compiler) {
        this.compiler = compiler;
    }


    public TypeUse onGrammar(DGrammarPattern p) {
        return CBuiltinLeafInfo.STRING;
    }

    public TypeUse onChoice(DChoicePattern p) {
        // can't support unions
        return CBuiltinLeafInfo.STRING;
    }

    public TypeUse onData(DDataPattern p) {
        return onDataType(p.getDatatypeLibrary(), p.getType());
    }

    public TypeUse onValue(DValuePattern p) {
        return onDataType(p.getDatatypeLibrary(),p.getType());
    }

    private TypeUse onDataType(String datatypeLibrary, String type) {
        DatatypeLib lib = compiler.datatypes.get(datatypeLibrary);
        if(lib!=null) {
            TypeUse use = lib.get(type);
            if(use!=null)
                return use;
        }

        // unknown
        return CBuiltinLeafInfo.STRING;
    }

    public TypeUse onInterleave(DInterleavePattern p) {
        return onContainer(p);
    }

    public TypeUse onGroup(DGroupPattern p) {
        return onContainer(p);
    }

    private TypeUse onContainer(DContainerPattern p) {
        TypeUse t=null;
        for( DPattern child : p ) {
            TypeUse s = child.accept(this);
            if(t!=null && t!=s)
                return CBuiltinLeafInfo.STRING; // heterogenous
            t = s;
        }
        return t;
    }

    public TypeUse onNotAllowed(DNotAllowedPattern p) {
        // TODO
        return error();
    }

    public TypeUse onEmpty(DEmptyPattern p) {
        return CBuiltinLeafInfo.STRING;
    }

    public TypeUse onList(DListPattern p) {
        return p.getChild().accept(this);
    }

    public TypeUse onOneOrMore(DOneOrMorePattern p) {
        return TypeUseFactory.makeCollection( p.getChild().accept(this) );
    }

    public TypeUse onZeroOrMore(DZeroOrMorePattern p) {
        return TypeUseFactory.makeCollection( p.getChild().accept(this) );
    }

    public TypeUse onOptional(DOptionalPattern p) {
        return CBuiltinLeafInfo.STRING;
    }

    public TypeUse onRef(DRefPattern p) {
        // TODO: check for enums
        return p.getTarget().getPattern().accept(this);
    }

    public TypeUse onText(DTextPattern p) {
        return CBuiltinLeafInfo.STRING;
    }

//
//
// Not allowed in this context
//
//
    public TypeUse onAttribute(DAttributePattern p) {
        return error();
    }

    public TypeUse onElement(DElementPattern p) {
        return error();
    }

    public TypeUse onMixed(DMixedPattern p) {
        return error();
    }

    private TypeUse error() {
        throw new IllegalStateException();
    }
}
