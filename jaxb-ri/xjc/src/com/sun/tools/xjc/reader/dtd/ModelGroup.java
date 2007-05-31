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

package com.sun.tools.xjc.reader.dtd;

import java.util.ArrayList;
import java.util.List;

import com.sun.xml.dtdparser.DTDEventListener;


/**
 * @author Kohsuke Kawaguchi
 */
final class ModelGroup extends Term {
    enum Kind {
        CHOICE, SEQUENCE
    }

    Kind kind;

    private final List<Term> terms = new ArrayList<Term>();

    void normalize(List<Block> r, boolean optional) {
        switch(kind) {
        case SEQUENCE:
            for( Term t : terms )
                t.normalize(r,optional);
            return;
        case CHOICE:
            Block b = new Block(isOptional()||optional,isRepeated());
            addAllElements(b);
            r.add(b);
            return;
        }
    }

    void addAllElements(Block b) {
        for( Term t : terms )
            t.addAllElements(b);
    }

    boolean isOptional() {
        switch(kind) {
        case SEQUENCE:
            for( Term t : terms )
                if(!t.isOptional())
                    return false;
            return true;
        case CHOICE:
            for( Term t : terms )
                if(t.isOptional())
                    return true;
            return false;
        default:
            throw new IllegalArgumentException();
        }
    }

    boolean isRepeated() {
        switch(kind) {
        case SEQUENCE:
            return true;
        case CHOICE:
            for( Term t : terms )
                if(t.isRepeated())
                    return true;
            return false;
        default:
            throw new IllegalArgumentException();
        }
    }

    void setKind(short connectorType) {
        Kind k;
        switch(connectorType) {
        case DTDEventListener.SEQUENCE:
            k = Kind.SEQUENCE;
            break;
        case DTDEventListener.CHOICE:
            k = Kind.CHOICE;
            break;
        default:
            throw new IllegalArgumentException();
        }

        assert kind==null || k==kind;
        kind = k;
    }

    void addTerm(Term t) {
        if (t instanceof ModelGroup) {
            ModelGroup mg = (ModelGroup) t;
            if(mg.kind==this.kind) {
                terms.addAll(mg.terms);
                return;
            }
        }
        terms.add(t);
    }


    Term wrapUp() {
        switch(terms.size()) {
        case 0:
            return EMPTY;
        case 1:
            assert kind==null;
            return terms.get(0);
        default:
            assert kind!=null;
            return this;
        }
    }

}
