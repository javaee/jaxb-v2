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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents one strongly-connected component
 * of the {@link Element} graph.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ConnectedComponent implements Iterable<Element> {
    /**
     * {@link Element}s that belong to this component.
     */
    private final List<Element> elements = new ArrayList<Element>();

    /*package*/ boolean isRequired;

    /**
     * Returns true iff this {@link ConnectedComponent}
     * can match a substring whose length is greater than 1.
     *
     * <p>
     * That means this property will become a collection property.
     */
    public final boolean isCollection() {
        assert !elements.isEmpty();

        // a strongly connected component by definition has a cycle,
        // so if its size is bigger than 1 there must be a cycle.
        if(elements.size()>1)
            return true;

        // if size is 1, it might be still forming a self-cycle
        Element n = elements.get(0);
        return n.hasSelfLoop();
    }

    /**
     * Returns true iff this {@link ConnectedComponent}
     * forms a cut set of a graph.
     *
     * <p>
     * That means any valid element sequence must have at least
     * one value for this property.
     */
    public final boolean isRequired() {
        return isRequired;
    }

    /*package*/void add(Element e) {
        assert !elements.contains(e);
        elements.add(e);
    }

    public Iterator<Element> iterator() {
        return elements.iterator();
    }

    /**
     * Just produces debug representation
     */
    public String toString() {
        String s = elements.toString();
        if(isRequired())
            s += '!';
        if(isCollection())
            s += '*';
        return s;
    }
}
