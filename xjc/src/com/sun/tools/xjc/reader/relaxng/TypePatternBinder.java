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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DListPattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

/**
 * Fumigate the named patterns that can be bound to inheritance.
 *
 * @author Kohsuke Kawaguchi
 */
final class TypePatternBinder extends DPatternWalker {
    private boolean canInherit;
    private final Stack<Boolean> stack = new Stack<Boolean>();

    /**
     * Patterns that are determined not to be bindable to inheritance.
     */
    private final Set<DDefine> cannotBeInherited = new HashSet<DDefine>();


    void reset() {
        canInherit = true;
        stack.clear();
    }

    public Void onRef(DRefPattern p) {
        if(!canInherit) {
            cannotBeInherited.add(p.getTarget());
        } else {
            // if the whole pattern is like "A,B", we can only inherit from
            // either A or B. For now, always derive from A.
            // it might be worthwhile to have a smarter binding logic where
            // we pick A and B based on their 'usefulness' --- by taking into
            // account how many other paterns are derived from those.
            canInherit = false;
        }
        return null;
    }

    /*
        Set the flag to false if we hit a pattern that cannot include
        a <ref> to be bound as an inheritance.

        All the following code are the same
    */
    public Void onChoice(DChoicePattern p) {
        push(false);
        super.onChoice(p);
        pop();
        return null;
    }

    public Void onAttribute(DAttributePattern p) {
        push(false);
        super.onAttribute(p);
        pop();
        return null;
    }

    public Void onList(DListPattern p) {
        push(false);
        super.onList(p);
        pop();
        return null;
    }

    public Void onMixed(DMixedPattern p) {
        push(false);
        super.onMixed(p);
        pop();
        return null;
    }

    public Void onOneOrMore(DOneOrMorePattern p) {
        push(false);
        super.onOneOrMore(p);
        pop();
        return null;
    }

    public Void onZeroOrMore(DZeroOrMorePattern p) {
        push(false);
        super.onZeroOrMore(p);
        pop();
        return null;
    }

    public Void onOptional(DOptionalPattern p) {
        push(false);
        super.onOptional(p);
        pop();
        return null;
    }

    private void push(boolean v) {
        stack.push(canInherit);
        canInherit = v;
    }

    private void pop() {
        canInherit = stack.pop();
    }
}
