package com.sun.xml.bind.v2.util;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class CollisionCheckStackTest extends TestCase {
    public void test1() {
        CollisionCheckStack<String> s = new CollisionCheckStack<String>();
        assertFalse(s.push("foo"));
        assertFalse(s.push("bar"));
        s.pop();
        assertFalse(s.push("bar"));
        s.pop();
        assertFalse(s.push("baz"));
        s.pop();
        assertTrue( s.push("foo"));
    }

    /**
     * Tests the reallocation and reset.
     */
    public void test2() {
        CollisionCheckStack<Integer> s = new CollisionCheckStack<Integer>();

        for( int j=0; j<3; j++ ) {
            for( int i=0; i<100; i++ )
                assertFalse(s.push(i));
            assertTrue(s.push(5));
            s.reset();
        }
    }
}
