package org.kohsuke.rngom.digested;

import java.util.Iterator;


/**
 * A pattern that can contain other patterns.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class DContainerPattern extends DPattern implements Iterable<DPattern> {
    private DPattern head;
    private DPattern tail;

    public DPattern firstChild() {
        return head;
    }

    public DPattern lastChild() {
        return tail;
    }

    public int countChildren() {
        int i=0;
        for( DPattern p=firstChild(); p!=null; p=p.next)
            i++;
        return i;
    }

    public Iterator<DPattern> iterator() {
        return new Iterator<DPattern>() {
            DPattern next = head;
            public boolean hasNext() {
                return next!=null;
            }

            public DPattern next() {
                DPattern r = next;
                next = next.next;
                return r;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    void add( DPattern child ) {
        if(tail==null) {
            child.prev = child.next = null;
            head = tail = child;
        } else {
            child.prev = tail;
            tail.next = child;
            child.next = null;
            tail = child;
        }
    }
}
