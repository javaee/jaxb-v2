/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.txw2.model;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
final class CycleIterator implements Iterator<Leaf> {
    private Leaf start;
    private Leaf current;
    private boolean hasNext = true;

    public CycleIterator(Leaf start) {
        assert start!=null;
        this.start = start;
        this.current = start;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public Leaf next() {
        Leaf last = current;
        current = current.getNext();
        if(current==start)
            hasNext = false;

        return last;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
