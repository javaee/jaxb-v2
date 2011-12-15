/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.tools.xjc.reader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 *
 * @author snajper
 */
public class RingJUTest extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(RingJUTest.class);
    }

    public void test1() throws InterruptedException {
        Ring r = Ring.get();
        assertNull(r);
        r = Ring.begin();
        assertNull(r);
        Ring.end(r);
        r = Ring.begin();
        assertNull(r);
        Ring.end(r);
        r = Ring.get();
        assertNull(r);
    }
    
}
