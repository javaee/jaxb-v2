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

package com.sun.tools.xjc.reader.gbind;

import java.util.LinkedHashSet;

/**
 * Factory methods for {@link ElementSet}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ElementSets {
    /**
     * Returns an union of two {@link ElementSet}s.
     *
     * This method performs better if lhs is bigger than rhs
     */
    public static ElementSet union(ElementSet lhs, ElementSet rhs) {
        if(lhs.contains(rhs))
            return lhs;
        if(lhs==ElementSet.EMPTY_SET)
            return rhs;
        if(rhs==ElementSet.EMPTY_SET)
            return lhs;
        return new MultiValueSet(lhs,rhs);
    }

    /**
     * {@link ElementSet} that has multiple {@link Element}s in it.
     *
     * This isn't particularly efficient or anything, but it will do for now.
     */
    private static final class MultiValueSet extends LinkedHashSet<Element> implements ElementSet {
        public MultiValueSet(ElementSet lhs, ElementSet rhs) {
            addAll(lhs);
            addAll(rhs);
            // not that anything will break with size==1 MultiValueSet,
            // but it does suggest that we are missing an easy optimization
            assert size()>1;
        }

        private void addAll(ElementSet lhs) {
            if(lhs instanceof MultiValueSet) {
                super.addAll((MultiValueSet)lhs);
            } else {
                for (Element e : lhs)
                    add(e);
            }
        }

        public boolean contains(ElementSet rhs) {
            // this isn't complete but sound
            return super.contains(rhs) || rhs==ElementSet.EMPTY_SET;
        }

        public void addNext(Element element) {
            for (Element e : this)
                e.addNext(element);
        }
    }
}
