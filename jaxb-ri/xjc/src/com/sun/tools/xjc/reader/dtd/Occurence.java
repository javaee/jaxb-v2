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

import java.util.List;

import com.sun.xml.dtdparser.DTDEventListener;


/**
 * @author Kohsuke Kawaguchi
 */
final class Occurence extends Term {
    final Term term;
    final boolean isOptional;
    final boolean isRepeated;

    Occurence(Term term, boolean optional, boolean repeated) {
        this.term = term;
        isOptional = optional;
        isRepeated = repeated;
    }

    static Term wrap( Term t, int occurence ) {
        switch(occurence) {
        case DTDEventListener.OCCURENCE_ONCE:
            return t;
        case DTDEventListener.OCCURENCE_ONE_OR_MORE:
            return new Occurence(t,false,true);
        case DTDEventListener.OCCURENCE_ZERO_OR_MORE:
            return new Occurence(t,true,true);
        case DTDEventListener.OCCURENCE_ZERO_OR_ONE:
            return new Occurence(t,true,false);
        default:
            throw new IllegalArgumentException();
        }
    }

    void normalize(List<Block> r, boolean optional) {
        if(isRepeated) {
            Block b = new Block(isOptional||optional,true);
            addAllElements(b);
            r.add(b);
        } else {
            term.normalize(r,optional||isOptional);
        }
    }

    void addAllElements(Block b) {
        term.addAllElements(b);
    }

    boolean isOptional() {
        return isOptional||term.isOptional();
    }

    boolean isRepeated() {
        return isRepeated||term.isRepeated();
    }
}
